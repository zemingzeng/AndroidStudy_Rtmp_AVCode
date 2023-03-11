//
// Created by Admin on 2021/1/29.
//
#include <malloc.h>
#include <jni.h>
#include "faac_audio_encode.h"


FAACAudioEncode::FAACAudioEncode() {

}

void FAACAudioEncode::getFaacVersion(char *id, char *copyRight) {

    int ret = faacEncGetVersion(&id, &copyRight);

    log("faacEncGetVersion ret : %d", ret);

}


void FAACAudioEncode::openFaacEncode(int sampleRate, int channelCount) {

    // 输入原始数据的大小 通道数+采样率+采样位数可以确定其值）
    unsigned long inputSamples;

    // 打开编码器
    audioEncoder = faacEncOpen(sampleRate, channelCount,
                               &inputSamples, &encodedDataMaxBytes);
    log("openFaacEncode inputSamples : %d encodedDataMaxBytes : %d ", inputSamples,
        encodedDataMaxBytes);

    //实例化编码好的数据的容器
    encodedData = (unsigned char *) calloc(1, encodedDataMaxBytes);

    //所有通道采样点数，每个点*采样位数/8(16/8)就是字节数
    inputDataSize = inputSamples*2;

    //获取编码参数
    faacEncConfigurationPtr config = faacEncGetCurrentConfiguration(audioEncoder);

    //编码等级
    config->aacObjectType = LOW;
    //mpeg 1、MPEG2是大多是应用在DVD上的。
    //     2、MPEG4就是MP4大多是应用在手机视频上的。
    config->mpegVersion = MPEG4;

    //Bitstream output format (0 = Raw 裸流 ; 1 = ADTS 带头信息)
    config->outputFormat = 0;

    //16位采样
    config->inputFormat = FAAC_INPUT_16BIT;

    //编码器配置参数
    faacEncSetConfiguration(audioEncoder, config);

}


int FAACAudioEncode::getInputDataSize() {

    return inputDataSize;

}

void FAACAudioEncode::encode(int32_t *pcmData, jint dataLength) {

    log("audio encoder state :%d ", audioEncoder);

    if (!audioEncoder) {
        return;
    }

    int aacDataLength = faacEncEncode(audioEncoder, pcmData, dataLength, encodedData,
                                      encodedDataMaxBytes);

    log("faac encoded data length : %d", aacDataLength);
    if (aacDataLength > 0) {

        if (startTime == 0) {
            startTime = RTMP_GetTime();
        }

        //queue aac packet
        queueAacData(encodedData, aacDataLength);
    }
}

void FAACAudioEncode::queueAacData(unsigned char *data, int dataLength) {

    if (rtmp && packetQueue) {

        log("queueAacData dataLength:%d", dataLength);

        // 多个两个字节0xaf 0x00 头文件数据
        // 多个两个字节0xaf 0x01 音频数据
        int packetBodySize = dataLength + 2;

        RTMPPacket *aacPacket = new RTMPPacket;
        RTMPPacket_Alloc(aacPacket, packetBodySize);
        RTMPPacket_Reset(aacPacket);
        //7个基本信息赋值

        aacPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        aacPacket->m_nBodySize = packetBodySize;
        //id 视频 音频发送packet不能相同
        aacPacket->m_nChannel = 11;
        aacPacket->m_headerType = RTMP_PACKET_SIZE_LARGE;
        aacPacket->m_hasAbsTimestamp = 0;
        aacPacket->m_nTimeStamp = RTMP_GetTime() - startTime;
        aacPacket->m_nInfoField2 = rtmp->m_stream_id;

        aacPacket->m_body[0] = 0xaf;
        aacPacket->m_body[1] = 0x01;

        memcpy(&aacPacket->m_body[2], data, dataLength);


        packetQueue->push(aacPacket);

    }

}


void FAACAudioEncode::setQueue(SafeQueue<RTMPPacket *> *packetQueue) {
    this->packetQueue = packetQueue;
}

void FAACAudioEncode::setRtmp(RTMP *rtmp) {
    this->rtmp = rtmp;
}

void FAACAudioEncode::queueAccDataHeader() {

    if (rtmp && packetQueue && audioEncoder) {

        if (startTime == 0) {
            startTime = RTMP_GetTime();
        }

//        u_long aacHeaderDataLength = 2;
//        u_char aacHeaderData[2] = {0x12, 0x08};

        u_long aacHeaderDataLength;
        u_char *aacHeaderData;
        int ret = faacEncGetDecoderSpecificInfo(audioEncoder, &aacHeaderData, &aacHeaderDataLength);
        log("queueAccDataHeader get header data ret : %d  aac header data length: %d", ret,
            aacHeaderDataLength);


        int packetBodySize = aacHeaderDataLength + 2;
        RTMPPacket *aacHeaderPacket = new RTMPPacket;
        RTMPPacket_Alloc(aacHeaderPacket, packetBodySize);
        RTMPPacket_Reset(aacHeaderPacket);

        //7个基本信息赋值

        aacHeaderPacket->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        aacHeaderPacket->m_nBodySize = packetBodySize;
        //id 视频 音频发送packet不能相同
        aacHeaderPacket->m_nChannel = 11;
        aacHeaderPacket->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
        aacHeaderPacket->m_hasAbsTimestamp = 0;
        aacHeaderPacket->m_nTimeStamp = 0;
        aacHeaderPacket->m_nInfoField2 = rtmp->m_stream_id;

        aacHeaderPacket->m_body[0] = 0xaf;
        aacHeaderPacket->m_body[1] = 0x00;

        memcpy(&aacHeaderPacket->m_body[2], aacHeaderData, aacHeaderDataLength);


        packetQueue->push(aacHeaderPacket);

    }

}

FAACAudioEncode::~FAACAudioEncode() {

    if (packetQueue) {
        packetQueue = nullptr;
    }

    if (rtmp) {
        rtmp = nullptr;
    }

    if (audioEncoder) {
        faacEncClose(audioEncoder);
        audioEncoder = nullptr;
    }

}



