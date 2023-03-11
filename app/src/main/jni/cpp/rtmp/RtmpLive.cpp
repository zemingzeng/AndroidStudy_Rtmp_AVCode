//
// Created by Admin on 2021/1/25.
//


#include "../../head/rtmp/rtmp_live.h"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"22m",__VA_ARGS__)

//声明定义全局的rtmp数据结构
struct RtmpLive {

    //rtmp
    RTMP *rtmp;

    //sps长度 2个字节 rtmp协议规定用两字节(Sequence Parameter Set)
    int16_t sps_data_length;
    int8_t *sps_data;

    //pps长度 2个字节 rtmp协议规定用两字节(Picture Parameter Set/Video Parameter Set--vps)
    int16_t pps_data_length;
    int8_t *pps_data;

};
RtmpLive *rtmpLive = nullptr;

//声明函数
int sendPacket(RTMPPacket *pPacket, RtmpLive *rtmpLive);

void sendVideoEncodeData(int8_t *data, int dataLength, int time);

void saveSPSAndPPS(int8_t *data, int dataLength, RtmpLive *rtmpLive);

RTMPPacket *createSPSPPSPacket(RtmpLive *rtmpLive);

RTMPPacket *createFramePacket(RtmpLive *pLive, int8_t *data, int length, int time);

void sendAudioEncodeData(jbyte *data, jint datalength, jlong time, jint type);

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_zzm_play_rtmp_MyMediaProjection_connect(JNIEnv *env, jobject thiz, jstring url_) {

    const char *url = env->GetStringUTFChars(url_, nullptr);

    int ret;
    do {

        LOGI("jni connect url : %s", url);
        LOGI("jni sizeof  struct RtmpLive : %d", sizeof(RtmpLive));

        //初始化 实例化 rtmp 建立链接
        rtmpLive = (RtmpLive *) (malloc(sizeof(RtmpLive)));
        memset(rtmpLive, 0, sizeof(RtmpLive));

        rtmpLive->rtmp = RTMP_Alloc();
        RTMP_Init(rtmpLive->rtmp);

        //10s 超时
        rtmpLive->rtmp->Link.timeout = 10;

        //url
        ret = RTMP_SetupURL(rtmpLive->rtmp, (char *) url);
        if (!ret) break;

        RTMP_EnableWrite(rtmpLive->rtmp);

        LOGI("jni RTMP_Connect");
        ret = RTMP_Connect(rtmpLive->rtmp, 0);
        if (!ret) break;

        LOGI("jni RTMP_ConnectStream");
        ret = RTMP_ConnectStream(rtmpLive->rtmp, 0);
        if (!ret) break;
        LOGI("jni rtmp connect success");

    } while (0);

    //链接失败则释放
    if (!ret && rtmpLive) {
        free(rtmpLive);
        rtmpLive = nullptr;
    }

    env->ReleaseStringUTFChars(url_, url);

    return ret;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_rtmp_MyMediaProjection_sendData(JNIEnv *env, jobject thiz, jbyteArray data_,
                                           jint data_length, jlong time, jint type) {
    //LOGI("jni sendData");
    jbyte *data = env->GetByteArrayElements(data_, nullptr);

    if (type == 0) {
        //编码的视频数据
        sendVideoEncodeData(data, data_length, time);
    } else if (type == 1 || type == 2) {
        //1 音频头数据 2 编码的音频数据
        sendAudioEncodeData(data, data_length, time, type);
    }


    env->ReleaseByteArrayElements(data_, data, 0);
}

int sendPacket(RTMPPacket *rtmpPacket, RtmpLive *rtmpLive) {

    int ret;
    ret = RTMP_SendPacket(rtmpLive->rtmp, rtmpPacket, 1);
    RTMPPacket_Free(rtmpPacket);
    free(rtmpPacket);
    return ret;

}

void sendAudioEncodeData(jbyte *data, jint datalength, jlong time, jint type) {

    // 多个两个字节0xaf 0x00 头文件数据
    // 多个两个字节0xaf 0x01 音频数据
    int packetBodySize = datalength + 2;

    RTMPPacket *rtmpPacket = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    RTMPPacket_Alloc(rtmpPacket, packetBodySize);

    //7个基本信息赋值

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    rtmpPacket->m_nBodySize = packetBodySize;
    //id 视频 音频发送packet不能相同
    rtmpPacket->m_nChannel = 5;
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nTimeStamp = time;
    rtmpPacket->m_nInfoField2 = rtmpLive->rtmp->m_stream_id;

    rtmpPacket->m_body[0] = 0xaf;
    if (type == 1) {
        rtmpPacket->m_body[1] = 0x00;
    } else if (type == 2) {
        rtmpPacket->m_body[1] = 0x01;
    }

    memcpy(&rtmpPacket->m_body[2], data, datalength);
    int ret = sendPacket(rtmpPacket, rtmpLive);
    LOGI("jni sendAudioPacket ret :%d  data length:%d  time:%d", ret, datalength, time);
}

void sendVideoEncodeData(int8_t *data, int dataLength, int time) {
    //发送编码的视频数据
    int ret;
    if (data[4] == 0x67) {
        //是sps pps 存储到全局变量
        if (rtmpLive && (!rtmpLive->sps_data || !rtmpLive->pps_data)) {
            saveSPSAndPPS(data, dataLength, rtmpLive);
            LOGI("jni saveSPSAndPPS");
        }
        return;
    }

    if (data[4] == 0x65) {
        //I 帧 先发送pps sps 数据
        RTMPPacket *rtmpPacket = createSPSPPSPacket(rtmpLive);
        ret = sendPacket(rtmpPacket, rtmpLive);
        LOGI("jni sendSPSPPSPacket ret :%d data length:%d  time:%d", ret, dataLength, time);
    }

    RTMPPacket *rtmpPacket1 = createFramePacket(rtmpLive, data, dataLength, time);
    ret = sendPacket(rtmpPacket1, rtmpLive);
    LOGI("jni sendFramePacket ret :%d  data length:%d  time:%d", ret, dataLength, time);
}


void saveSPSAndPPS(int8_t *data, int dataLength, RtmpLive *rtmpLive) {
    LOGI("jni sps pps data length : %d ", dataLength);
    for (int i = 0; i < dataLength; i++) {

        if (i + 4 < dataLength) {

            if (data[i] == 0x00 &&
                data[i + 1] == 0x00 &&
                data[i + 2] == 0x00 &&
                data[i + 3] == 0x01 &&
                data[i + 4] == 0x68) {
                rtmpLive->sps_data_length = i - 4;
                LOGI("jni sps_data_length : %d ", rtmpLive->sps_data_length);

                rtmpLive->pps_data_length = dataLength - 8 - rtmpLive->sps_data_length;
                LOGI("jni pps_data_length : %d ", rtmpLive->pps_data_length);

                rtmpLive->sps_data = (int8_t *) malloc(rtmpLive->sps_data_length);
                rtmpLive->pps_data = (int8_t *) malloc(rtmpLive->pps_data_length);

                memcpy(rtmpLive->sps_data, data + 4, rtmpLive->sps_data_length);
                memcpy(rtmpLive->pps_data, data + rtmpLive->sps_data_length + 8,
                       rtmpLive->pps_data_length);
                break;
            }
        }

    }

}

RTMPPacket *createSPSPPSPacket(RtmpLive *rtmpLive) {

    //16->rtmp协议固定内容加上描述sps pps长度等信息共有16字节
    int packetBodySize = rtmpLive->sps_data_length + rtmpLive->pps_data_length + 16;

    RTMPPacket *rtmpPacket = (RTMPPacket *) malloc(sizeof(RTMPPacket));
    //给packet body分配内存
    RTMPPacket_Alloc(rtmpPacket, packetBodySize);

    //7个基本信息赋值

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    rtmpPacket->m_nBodySize = packetBodySize;
    //id 视频 音频发送packet不能相同
    rtmpPacket->m_nChannel = 4;
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nTimeStamp = 0;
    rtmpPacket->m_nInfoField2 = rtmpLive->rtmp->m_stream_id;

    //赋值body
    int i = 0;
    //固定的5个字节 avc header等表示信息
    rtmpPacket->m_body[i++] = 0x17;
    rtmpPacket->m_body[i++] = 0x00;
    rtmpPacket->m_body[i++] = 0x00;
    rtmpPacket->m_body[i++] = 0x00;
    rtmpPacket->m_body[i++] = 0x00;
    //版本号
    rtmpPacket->m_body[i++] = 0x01;
    //sps 前三个字节 profile等信息（编码等级等等）
    rtmpPacket->m_body[i++] = rtmpLive->sps_data[1];
    rtmpPacket->m_body[i++] = rtmpLive->sps_data[2];
    rtmpPacket->m_body[i++] = rtmpLive->sps_data[3];
    //固定的俩个字节表示几个字节表示NALU长度和sps个数
    rtmpPacket->m_body[i++] = 0xff;
    rtmpPacket->m_body[i++] = 0xe1;
    //sps长度 信息 用这两个字节表示 先高八位后第八位 无分隔符
    rtmpPacket->m_body[i++] = (rtmpLive->sps_data_length >> 8) & 0xff;
    rtmpPacket->m_body[i++] = rtmpLive->sps_data_length & 0xff;
    //copy sps 内容
    memcpy(&rtmpPacket->m_body[i], rtmpLive->sps_data, rtmpLive->sps_data_length);
    i += rtmpLive->sps_data_length;
    //pps 数量 1
    rtmpPacket->m_body[i++] = 0x01;
    //两个字节表示pps长度 先高八位后第八位  无分隔符
    rtmpPacket->m_body[i++] = (rtmpLive->pps_data_length >> 8) & 0xff;;
    rtmpPacket->m_body[i++] = rtmpLive->pps_data_length & 0xff;
    //copy pps内容
    memcpy(&rtmpPacket->m_body[i], rtmpLive->pps_data, rtmpLive->pps_data_length);

    return rtmpPacket;
}

RTMPPacket *createFramePacket(RtmpLive *rtmpLive, int8_t *data, int dataLength, int time) {

    //除去4个字节的分隔符
    data += 4;
    dataLength -= 4;

    //9-> 5个固定字节内容 4个字节表示数据长度 从高位到低位
    int packetBodySize = dataLength + 9;

    RTMPPacket *rtmpPacket = (RTMPPacket *) malloc(sizeof(RTMPPacket));

    RTMPPacket_Alloc(rtmpPacket, packetBodySize);

    //7个基本信息赋值

    rtmpPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    rtmpPacket->m_nBodySize = packetBodySize;
    //id 视频 音频发送packet不能相同 不能太大如0xaa 会崩溃
    rtmpPacket->m_nChannel = 4;
    rtmpPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
    rtmpPacket->m_hasAbsTimestamp = 0;
    rtmpPacket->m_nTimeStamp = time;
    rtmpPacket->m_nInfoField2 = rtmpLive->rtmp->m_stream_id;

    //packet body赋值

    //前5个固定字节内容
    rtmpPacket->m_body[0] = 0x27;
    if (data[0] == 0x65) {
        //I 帧
        rtmpPacket->m_body[0] = 0x17;
    }
    rtmpPacket->m_body[1] = 0x01;
    rtmpPacket->m_body[2] = 0x00;
    rtmpPacket->m_body[3] = 0x00;
    rtmpPacket->m_body[4] = 0x00;
    //4个字节表示data的长度 无隔符
    rtmpPacket->m_body[5] = (dataLength >> 24) & 0xff;
    rtmpPacket->m_body[6] = (dataLength >> 16) & 0xff;
    rtmpPacket->m_body[7] = (dataLength >> 8) & 0xff;
    rtmpPacket->m_body[8] = dataLength & 0xff;
    // frame 数据copy
    memcpy(&rtmpPacket->m_body[9], data, dataLength);

    return rtmpPacket;
}
