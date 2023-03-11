//
// Created by Admin on 2021/1/27.
//

#include "x264_video_encode.h"

int X264VideoEncode::x264initAndOpen(int w, int h, int fps, int bitRate) {

    //初始化x264 参数 并且 open

    width = w;
    height = h;
    ySize = w * h;
    uvSize = ySize / 4;
    this->fps = fps;
    this->bitRate = bitRate;

    if (x264Encode) {
        x264_encoder_close(x264Encode);
        x264Encode = nullptr;
    }

    //定义编码器的编码参数
    x264_param_t param;
    //preset 越快编码质量比较差，直播合适了 tune差不多的意思
    x264_param_default_preset(&param, x264_preset_names[0], x264_tune_names[1]);
    param.i_width = width;
    param.i_height = height;
    //编码等级
    /**
     *  10	    (supports only QCIF format and below with 380160 samples/sec)
        11	    (CIF and below. 768000 samples/sec)
        12	    (CIF and below. 1536000 samples/sec)
        13	    (CIF and below. 3041280 samples/sec)
        20	    (CIF and below. 3041280 samples/sec)
        21	    (Supports HHR formats. Enables Interlace support. 5068800 samples/sec)
        22	    (Supports SD/4CIF formats. Enables Interlace support. 5184000 samples/sec)
        30	    (Supports SD/4CIF formats. Enables Interlace support. 10368000 samples/sec)
        31	    (Supports 720p HD format. Enables Interlace support. 27648000 samples/sec)
        32	    (Supports SXGA format. Enables Interlace support. 55296000 samples/sec)
        40	    (Supports 2Kx1K format. Enables Interlace support. 62914560 samples/sec)
        41      (Supports 2Kx1K format. Enables Interlace support. 62914560 samples/sec)
        42	    (Supports 2Kx1K format. Frame coding only. 125829120 samples/sec)
        50	    (Supports 3672x1536 format. Frame coding only. 150994944 samples/sec)
        51	    (Supports 4096x2304 format. Frame coding only. 251658240 samples/sec)
     */
    param.i_level_idc = 32;
    //编码数据格式 yu12 yy uu vv
    param.i_csp = X264_CSP_I420;
    //是否编码B帧
    param.i_bframe = 0;
    //平均码率 折中选择
    param.rc.i_rc_method = X264_RC_ABR;
    //码率 K为单位
    param.rc.i_bitrate = bitRate / 1024;
    //帧率分母
    param.i_fps_num = fps;
    //帧率分子
    param.i_fps_den = 1;
    param.i_timebase_den = param.i_fps_den;
    param.i_timebase_num = param.i_fps_num;
    //不是用时间戳来记录帧间距
    // VFR input.  If 1, use timebase and timestamps for ratecontrol purposes
    param.b_vfr_input = 0;
    //I 帧间隔 多少秒编一个I帧 (每两秒一个I帧)
    // Force an IDR keyframe at this interval
    param.i_keyint_max = fps * 2;
    //是否所有I帧前面都添加sps pps信息
    // put SPS/PPS before each keyframe
    param.b_repeat_headers = 1;
    //encode multiple frames in parallel
    //是否用多线程编码
    param.i_threads = 1;

    //apply param
    //x264_profile_names编码质量
    /**
     *
     *      66	Baseline
            77	Main  x264_profile_names[1]
            88	Extended
            100	High (FRExt)
            110	High 10 (FRExt)
            122	High 4:2:2 (FRExt)
            144	High 4:4:4 (FRExt)
     *
     */
    x264_param_apply_profile(&param, x264_profile_names[0]);

    int ret = 0;
    //打开编码器
    x264Encode = x264_encoder_open(&param);

    //初始化包装编码的data容量
    inputData = new x264_picture_t;
    ret = x264_picture_alloc(inputData, X264_CSP_I420, width, height);

    if (x264Encode && ret == 0) {
        ret = 1;
    }

    return ret;
}

void X264VideoEncode::encodeData(int8_t *data) {

    //copy y data
    memcpy(inputData->img.plane[0], data, ySize);

    //copy u data

    memcpy(inputData->img.plane[1], data + ySize, uvSize);

    //copy v data
    memcpy(inputData->img.plane[2], data + ySize + uvSize, uvSize);

    //编码了几个nalu (可初略理解为帧) 我们送进来的是一帧一帧的送所以应该永远为1
    int naluNumber;

    //编码出来的帧信息
    x264_picture_t outputDataInfo;

    //编码出来的压缩数据
    x264_nal_t *outputData;

    int ret = x264_encoder_encode(x264Encode, &outputData, &naluNumber, inputData, &outputDataInfo);

    log("264_encoder_encode ret : %d ", ret);
    log("x264_encoder_encode naluNumber : %d  ", naluNumber);

    for (int i = 0; i < naluNumber; ++i) {
        log("x264_encoder_encode 第 : %d  个 nalu , nalu type : %d", i, outputData[i].i_type);
        //outputData[i].p_payload 编码出来的数据
        //outputData[i].i_payload 编码出来的数据的长度
        //naluNumber: 编码出来的单元数据个数 第一次应该为4 （sps pps sei I）
        //        callbackFromJniUtil->x264EncodeDataCallback(
        //                "getDataFromJni", "([B)V",
        //                (char *) outputData[i].p_payload,
        //                outputData[i].i_payload);
    }

    //包装videoPacket 加入 packet queue中
    uint8_t spsData[100];
    uint8_t ppsData[100];
    int spsDataLength, ppsDataLength;

    if (naluNumber > 0) {

        int dataLength;
        uint8_t *data;
        int dataType;

        for (int i = 0; i < naluNumber; i++) {

            dataLength = outputData[i].i_payload;
            data = outputData[i].p_payload;
            dataType = outputData[i].i_type;

            //是I帧的时候 前面会输出sps和pps 先发送pps和sps
            if (NAL_SPS == dataType) {

                dataLength -= 4;
                spsDataLength = dataLength;
                memcpy(spsData, data + 4, spsDataLength);
                continue;

            } else if (NAL_PPS == dataType) {

                dataLength -= 4;
                ppsDataLength = dataLength;
                memcpy(ppsData, data + 4, ppsDataLength);
                //然后将pps和sps打包放入队列等待发送
                queueSPSAndPPSPacket(spsData, ppsData, spsDataLength, ppsDataLength);
                continue;

            }


            queueFramePacket(data, dataLength);

        }
    }

}


void X264VideoEncode::setCallbackFromJniUtil(CallbackFromJniUtil *callbackFromJniUtil_) {
    callbackFromJniUtil = callbackFromJniUtil_;
}

void X264VideoEncode::setQueue(SafeQueue<RTMPPacket *> *packetQueue) {
    this->packetQueue = packetQueue;
}

void X264VideoEncode::setRtmp(RTMP *rtmp) {
    this->rtmp = rtmp;
}

void X264VideoEncode::queueSPSAndPPSPacket(uint8_t *spsData,
                                           uint8_t *ppsData,
                                           int spsDataLength,
                                           int ppsDataLength) {

    if (packetQueue && rtmp) {

        log("queueSPSAndPPSPacket spsDataLength:%d  ppsDataLength:%d",
            spsDataLength,
            ppsDataLength);

        //16->rtmp协议固定内容加上描述sps pps长度等信息共有16字节
        int packetBodySize = spsDataLength + ppsDataLength + 16;

        RTMPPacket *SPSAndPPSPacket = new RTMPPacket;
        //给packet body分配内存
        RTMPPacket_Alloc(SPSAndPPSPacket, packetBodySize);
        RTMPPacket_Reset(SPSAndPPSPacket);

        //7个基本信息赋值
        SPSAndPPSPacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
        SPSAndPPSPacket->m_nBodySize = packetBodySize;
        //id 视频 音频发送packet不能相同
        SPSAndPPSPacket->m_nChannel = 10;
        SPSAndPPSPacket->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
        SPSAndPPSPacket->m_hasAbsTimestamp = 0;
        SPSAndPPSPacket->m_nTimeStamp = 0;
        SPSAndPPSPacket->m_nInfoField2 = rtmp->m_stream_id;

        //赋值body
        int i = 0;
        //固定的5个字节 avc header等表示信息
        SPSAndPPSPacket->m_body[i++] = 0x17;
        SPSAndPPSPacket->m_body[i++] = 0x00;
        SPSAndPPSPacket->m_body[i++] = 0x00;
        SPSAndPPSPacket->m_body[i++] = 0x00;
        SPSAndPPSPacket->m_body[i++] = 0x00;
        //版本号
        SPSAndPPSPacket->m_body[i++] = 0x01;
        //sps 前三个字节 profile等信息（编码等级等等）
        SPSAndPPSPacket->m_body[i++] = spsData[1];
        SPSAndPPSPacket->m_body[i++] = spsData[2];
        SPSAndPPSPacket->m_body[i++] = spsData[3];
        //固定的俩个字节表示几个字节表示NALU长度和sps个数
        SPSAndPPSPacket->m_body[i++] = 0xff;
        SPSAndPPSPacket->m_body[i++] = 0xe1;
        //sps长度 信息 用这两个字节表示 先高八位后第八位 无分隔符
        SPSAndPPSPacket->m_body[i++] = (spsDataLength >> 8) & 0xff;
        SPSAndPPSPacket->m_body[i++] = spsDataLength & 0xff;
        //copy sps 内容
        memcpy(&SPSAndPPSPacket->m_body[i], spsData, spsDataLength);
        i += spsDataLength;
        //pps 数量 1
        SPSAndPPSPacket->m_body[i++] = 0x01;
        //两个字节表示pps长度 先高八位后第八位  无分隔符
        SPSAndPPSPacket->m_body[i++] = (ppsDataLength >> 8) & 0xff;;
        SPSAndPPSPacket->m_body[i++] = ppsDataLength & 0xff;
        //copy pps内容
        memcpy(&SPSAndPPSPacket->m_body[i], ppsData, ppsDataLength);


        packetQueue->push(SPSAndPPSPacket);

    }
}

void X264VideoEncode::queueFramePacket(uint8_t *data, int dataLength) {

    if (packetQueue && rtmp) {

        if (startTime == 0) {
            startTime = RTMP_GetTime();
            log("开始推流的相对的时间 ：%d", startTime);
        }

        log("queueFramePacket dataLength:%d", dataLength);

        //除去分隔符
        if (data[2] == 0x00) {
            //0 0 0 1 65
            data += 4;
            dataLength -= 4;
        } else {
            //0 0  1 65
            data += 3;
            dataLength -= 3;
        }


        //9-> 5个固定字节内容 4个字节表示数据长度 从高位到低位
        int packetBodySize = dataLength + 9;

        RTMPPacket *framePacket = new RTMPPacket;
        RTMPPacket_Alloc(framePacket, packetBodySize);
        RTMPPacket_Reset(framePacket);

        //7个基本信息赋值

        framePacket->m_packetType = RTMP_PACKET_TYPE_VIDEO;
        framePacket->m_nBodySize = packetBodySize;
        //id 视频 音频发送packet不能相同 不能太大如0xaa 会崩溃
        framePacket->m_nChannel = 10;
        framePacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
        framePacket->m_hasAbsTimestamp = 0;
        uint32_t time = RTMP_GetTime() - startTime;
        log("queueFramePacket m_nTimeStamp : %d", time);
        framePacket->m_nTimeStamp = time;
        framePacket->m_nInfoField2 = rtmp->m_stream_id;

        //packet body赋值

        //前5个固定字节内容
        framePacket->m_body[0] = 0x27;
        if (data[0] == 0x65) {
            //I 帧
            framePacket->m_body[0] = 0x17;
        }
        framePacket->m_body[1] = 0x01;
        framePacket->m_body[2] = 0x00;
        framePacket->m_body[3] = 0x00;
        framePacket->m_body[4] = 0x00;
        //4个字节表示data的长度 无隔符
        framePacket->m_body[5] = (dataLength >> 24) & 0xff;
        framePacket->m_body[6] = (dataLength >> 16) & 0xff;
        framePacket->m_body[7] = (dataLength >> 8) & 0xff;
        framePacket->m_body[8] = dataLength & 0xff;
        // frame 数据copy
        memcpy(&framePacket->m_body[9], data, dataLength);

        packetQueue->push(framePacket);
    }

}

X264VideoEncode::~X264VideoEncode() {

    if (x264Encode) {
        log("~X264VideoEncode");
        x264_encoder_close(x264Encode);
        x264Encode = nullptr;
    }
    if (rtmp) {
        rtmp = nullptr;
    }
    if (packetQueue) {
        packetQueue->setWork(0);
        packetQueue->clear();
        packetQueue = nullptr;
    }
    if (callbackFromJniUtil) {
        callbackFromJniUtil = nullptr;
    }

}
