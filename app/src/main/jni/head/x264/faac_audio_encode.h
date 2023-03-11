//
// Created by Admin on 2021/1/29.
//

#ifndef PLAY_FAAC_AUDIO_ENCODE_H
#define PLAY_FAAC_AUDIO_ENCODE_H

#include <rtmp.h>
#include "faac.h"
#include "safe_queue.h"
#include "android_log.h"

class FAACAudioEncode {
public:
    FAACAudioEncode();

    ~FAACAudioEncode();

    void getFaacVersion(char *id, char *copyRight);

    void openFaacEncode(int sampleRate, int channelCount);

    int getInputDataSize();

    void encode(int32_t *pcmData, jint dataLength);

    void queueAacData(unsigned char *data, int length);

    void setQueue(SafeQueue<RTMPPacket *> *packetQueue);

    void setRtmp(RTMP *rtmp);

    void queueAccDataHeader();

public:

    faacEncHandle audioEncoder = nullptr;

    //编码出来的数据最大长度
    unsigned long encodedDataMaxBytes;

    unsigned char *encodedData = nullptr;

    unsigned long inputDataSize;

    RTMP *rtmp = nullptr;

    SafeQueue<RTMPPacket *> *packetQueue;

    int startTime = 0;
};

#endif //PLAY_FAAC_AUDIO_ENCODE_H
