package com.zzm.play.rtmp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import com.zzm.play.utils.l;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncode extends Thread {

    private MyMediaProjection myMediaProjection;
    private MediaCodec mediaCodec;
    private AudioRecord audioRecord;
    //采样率 一般为44.1k
    private int sampleRate = 44100;
    //通道数
    private int channelCount = 1;
    private int minBufferSize;

    public void startEncode(MyMediaProjection myMediaProjection) {

        this.myMediaProjection = myMediaProjection;

        try {

            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);

            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                    sampleRate, channelCount);

            //编码的出来的音频质量相关
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            //码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);

            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            //采样位数 采样率 通道数确定则 音频的大小就确定了
            minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            l.i("AudioRecord  minBufferSize ： " + minBufferSize);
            //采集麦克风的音频数据 单通道 16位
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            start();


        } catch (IOException e) {
            l.i(e.toString());
            e.printStackTrace();
        }

    }

    public void setWork(boolean work) {
        this.work = work;
    }

    private boolean work = false;

    @Override
    public void run() {

        mediaCodec.start();

        audioRecord.startRecording();

        byte[] temp = new byte[minBufferSize];
        byte[] temp1;
        ByteBuffer inputBuffer;
        ByteBuffer outputBuffer;

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        work = true;

        queueEncodeAudioHeaderData();

        while (work) {

            //读取pcm数据
            int dataLength = audioRecord.read(temp, 0, temp.length);

            if (dataLength <= 0)
                continue;

            //拿去编码
            int index = mediaCodec.dequeueInputBuffer(100);
            if (index >= 0) {

                inputBuffer = mediaCodec.getInputBuffer(index);
                inputBuffer.clear();
                inputBuffer.put(temp);

                mediaCodec.queueInputBuffer(index, 0,
                        dataLength, System.nanoTime() / 1000, 0);
                //jvm run time 纳秒
                //l.i(System.nanoTime()+"--------------------------------");
            }

            index = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);
            while (index >= 0 && work) {

                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs/1000;
                }

                outputBuffer = mediaCodec.getOutputBuffer(index);

                //处理编码好的音频数据
                temp1 = new byte[outputBuffer.remaining()];
                outputBuffer.get(temp1);
                //FileUtil.writeBytesTo16Chars(temp1, "audio_record.txt");

                queueEncodeAudioData(temp1, bufferInfo.presentationTimeUs/1000 - startTime);

                mediaCodec.releaseOutputBuffer(index, false);
                index = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            }
        }
        stopMe();

    }


    private long startTime = 0;
    private RTMPPacket rtmpPacket;

    private void queueEncodeAudioData(byte[] temp, long workTime) {

        rtmpPacket = new RTMPPacket(temp, temp.length, RTMPPacket.AUDIO_ENCODE_DATA, workTime);

        myMediaProjection.queueEncodeBuffer(rtmpPacket);

    }

    private void queueEncodeAudioHeaderData() {
        //发送编码的音频数据前先发送音频头文件（rtmp协议）
        byte[] temp = {0x12, 0x08};

        rtmpPacket = new RTMPPacket(temp, temp.length, RTMPPacket.AUDIO_HEADER_DATA, 0);

        myMediaProjection.queueEncodeBuffer(rtmpPacket);
    }


    private void stopMe() {
        startTime = 0;
        work = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
    }

    public void destroyMe() {
        stopMe();
        if (null != mediaCodec) {
            mediaCodec = null;
        }
        if (null != audioRecord) {
            audioRecord = null;
        }
    }

}
