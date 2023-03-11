//
// Created by Admin on 2021/1/27.
//

#ifndef PLAY_X264_VIDEO_ENCODE_H
#define PLAY_X264_VIDEO_ENCODE_H

#include <rtmp.h>
#include "x264.h"
#include "string"
#include "android_log.h"
#include "call_back_from_jni_util.h"
#include "safe_queue.h"

class X264VideoEncode {

public:
    ~X264VideoEncode();

    int x264initAndOpen(int w, int h, int fps, int bitRate);

    //编码相机帧数据
    void encodeData(int8_t *data);

    void setCallbackFromJniUtil(CallbackFromJniUtil *callbackFromJniUtil);

    void queueSPSAndPPSPacket(uint8_t *spsData,
                              uint8_t *ppsData,
                              int spsDataLength,
                              int ppsDataLength);

    void queueFramePacket(uint8_t *data, int dataLength);

    void setQueue(SafeQueue<RTMPPacket *> *packetQueue);

    void setRtmp(RTMP *rtmp);

private:
    //编码器
    x264_t *x264Encode = nullptr;
    //包装data的容器
    x264_picture_t *inputData = nullptr;
    int width;
    int height;
    //帧率
    int fps;
    //码率
    int bitRate;
    //一帧y数据的大小(yuv420)
    int ySize;
    //一帧u v数据的大小
    int uvSize;

    //jni回调java工具
    CallbackFromJniUtil *callbackFromJniUtil;

    //queue 用于加入队列
    SafeQueue<RTMPPacket *> *packetQueue;

    //rtmp
    RTMP *rtmp;

    //开始推流的时间 就是queue第一帧的时间
    uint32_t startTime = 0;

};

#endif //PLAY_X264_VIDEO_ENCODE_H
