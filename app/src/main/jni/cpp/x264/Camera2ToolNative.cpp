//
// Created by Admin on 2021/1/27.
//

#include "x264_video_encode.h"
#include <jni.h>
#include "pthread.h"
#include "string"
#include "safe_queue.h"
#include "faac_audio_encode.h"
//引入c头文件
extern "C" {
#include "rtmp.h"
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_testX264(JNIEnv *env, jobject thiz, jstring a) {

    const char *hello = env->GetStringUTFChars(a, 0);

    log("x264 test : %s", hello);
//    x264_param_t x264Param;
//    x264_t *x264 = x264_encoder_open(&x264Param);
//    if (x264) {
//        LOGI("x264_encoder_open success");
//    } else {
//        LOGI("x264_encoder_open failed");
//    }

    env->ReleaseStringUTFChars(a, hello);
}


void *threadRun(void *url);

//*&指针的引用
void releasePacket(RTMPPacket *&pPacket);

//全局的x264 encode
X264VideoEncode *x264VideoEncode = nullptr;

FAACAudioEncode *faacAudioEncode = nullptr;

RTMP *rtmp = nullptr;

SafeQueue<RTMPPacket *> packetQueue;

RTMPPacket *videoPacket = nullptr;

//是否链接好服务器了
bool alreadyTryConnect = false;

//是否可以推流了
bool readPushing = false;

//子线程指针标识符
pthread_t pThread;

//开始时间
uint32_t startTime;

//System.loadLibrary()就会调用此方法
CallbackFromJniUtil *callbackFromJniUtil = nullptr;
JavaVM *vm = nullptr;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm_, void *reserved) {
    vm = vm_;
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeX264EncodeInit(JNIEnv *env, jobject thiz) {

    x264VideoEncode = new X264VideoEncode;

    callbackFromJniUtil = new CallbackFromJniUtil(vm, env);

    callbackFromJniUtil->setClazz(thiz);

    x264VideoEncode->setCallbackFromJniUtil(callbackFromJniUtil);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_AudioRecorderTool_nativeFaacEncodeInit(JNIEnv *env, jobject thiz) {

    faacAudioEncode = new FAACAudioEncode;
//
//    char *id = (char *) calloc(1, 100);
//    char *copyRight = (char *) calloc(1, 100);
//    faacAudioEncode->getFaacVersion(id, copyRight);
//    log("FAAC version id : %s  copyRight : %s", id, copyRight);
//    free(id);
//    free(copyRight);
//    id = nullptr;
//    copyRight = nullptr;

}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zzm_play_x264_AudioRecorderTool_nativeFaacEncodeOpen(JNIEnv *env, jobject thiz,
                                                              jint sample_rate,
                                                              jint channel_count) {
    if (!faacAudioEncode)
        return -1;

    faacAudioEncode->openFaacEncode(sample_rate, channel_count);

    return faacAudioEncode->getInputDataSize();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeX264EncodeOpen(JNIEnv *env, jobject thiz, jint width,
                                                        jint height, jint fps, jint bit_rate) {

    if (x264VideoEncode) {

        int ret = x264VideoEncode->x264initAndOpen(width, height, fps, bit_rate);

        log("x264initAndOpen ret : %d", ret);
        log("x264initAndOpen width height fps bitRate : %d,%d,%d,%d", width, height, fps, bit_rate);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeRtmpstart(JNIEnv *env, jobject thiz, jstring url_) {

    //尝试链接过的不在链接
    if (alreadyTryConnect) {
        return;
    }

    const char *url = env->GetStringUTFChars(url_, 0);
    //要在子线程中链接服务器时使用所以的复制
    //要加个结尾符 ‘\0’
    char *url__ = new char[strlen(url) + 1];
    strcpy(url__, url);

    alreadyTryConnect = true;
    //开启子线程
    pthread_create(&pThread, 0, threadRun, url__);
    //threadRun(url__);
    env->ReleaseStringUTFChars(url_, url);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeSendPreviewData(JNIEnv *env, jobject thiz,
                                                         jbyteArray data_,
                                                         jint data_length) {
    log("java层接受到的推流原始数据length : %d", data_length);


    //把数据给到x264编码器编码
    if (!readPushing || !x264VideoEncode) {
        log("mingzz push not ready");
        return;
    }

    jbyte *data = env->GetByteArrayElements(data_, 0);


    x264VideoEncode->encodeData(data);

    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_AudioRecorderTool_sendAudioPcmData(JNIEnv *env, jobject thiz,
                                                          jbyteArray pcm_data, jint data_length) {

    if (!readPushing || !faacAudioEncode) {
        return;
    }

    jbyte *pcmData = env->GetByteArrayElements(pcm_data, 0);

    faacAudioEncode->encode((int32_t *) pcmData, data_length);

    env->ReleaseByteArrayElements(pcm_data, pcmData, 0);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeStop(JNIEnv *env, jobject thiz) {

    alreadyTryConnect = false;

    readPushing = false;

    startTime = 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_com_zzm_play_x264_Camera2Tool_nativeRelease(JNIEnv *env, jobject thiz) {

    if (rtmp) {
        log("rtmp close release");
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = nullptr;
    }

    if (x264VideoEncode) {
        delete x264VideoEncode;
        //析构函数里面自己释放x264编码器
        x264VideoEncode = nullptr;
    }

    if (faacAudioEncode) {
        delete faacAudioEncode;
        faacAudioEncode = nullptr;
    }

    if (callbackFromJniUtil) {
        delete callbackFromJniUtil;
        callbackFromJniUtil = nullptr;
    }


    alreadyTryConnect = false;

    readPushing = false;

    startTime = 0;


}


//链接服务器开始循环取编码数据的队列
void *threadRun(void *url_) {

    log("mingzz threadRun");

    char *url = (char *) url_;
    int ret = 0;
    do {

        rtmp = RTMP_Alloc();

        if (!rtmp) {
            log("mingzz 创建rtmp失败");
            break;
        }
        RTMP_Init(rtmp);

        //超时时间
        rtmp->Link.timeout = 10;

        //设置地址
        ret = RTMP_SetupURL(rtmp, url);
        log("mingzz rtmp 地址：%s", url);
        if (!ret) {
            log("mingzz rtmp设url失败");
            break;
        }

        //开启输出模式并连接
        RTMP_EnableWrite(rtmp);
        ret = RTMP_Connect(rtmp, 0);
        if (!ret) {
            log("mingzz rtmp connect 失败");
            break;
        }
        //连接好数据通道
        ret = RTMP_ConnectStream(rtmp, 0);
        if (!ret) {
            log("mingzz rtmp connect stream 失败");
            break;
        }

        log("mingzz rtmp 链接成功！");

        x264VideoEncode->setRtmp(rtmp);
        faacAudioEncode->setRtmp(rtmp);

        //可以开始获取队列里面的编码数据推流了
        readPushing = true;

        //开始推流的时间
        startTime = RTMP_GetTime();

        //从循环队列开始工作并且在去取编码数据
        packetQueue.setWork(1);

        x264VideoEncode->setQueue(&packetQueue);
        faacAudioEncode->setQueue(&packetQueue);
        faacAudioEncode->queueAccDataHeader();

        while (alreadyTryConnect) {

            packetQueue.pop(videoPacket);
            if (!alreadyTryConnect) {
                break;
            }
            if (!videoPacket) {
                break;
            }

            //推流
            log("从队列中取得数据并且发送 packet body size:%d packet type:%d", videoPacket->m_nBodySize,
                videoPacket->m_packetType);
            ret = RTMP_SendPacket(rtmp, videoPacket, 1);

            log("RTMP_SendPacket(rtmp, videoPacket, 1) ret :% d  ", ret);
            //推完后release掉
            releasePacket(videoPacket);
        }

        releasePacket(videoPacket);

    } while (0);

    if (rtmp && alreadyTryConnect) {
        log("rtmp close release");
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
        rtmp = nullptr;
    }

    return nullptr;

}

void releasePacket(RTMPPacket *&pPacket) {

    if (pPacket) {
        RTMPPacket_Free(pPacket);
        delete pPacket;
        pPacket = nullptr;
    }

}
