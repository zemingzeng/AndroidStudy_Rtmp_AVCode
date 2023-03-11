package com.zzm.play.rtmp;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.os.Bundle;

import com.zzm.play.utils.l;

import java.nio.ByteBuffer;

public class H26xEnCode extends Thread {

    private MediaCodec mediaCodec;
    private MyMediaProjection myMediaProjection;


    public void startEncode(MediaCodec mediaCodec, MyMediaProjection myMediaProjection) {

        this.mediaCodec = mediaCodec;

        this.myMediaProjection = myMediaProjection;

        start();

    }


    public void setWork(boolean work) {
        this.work = work;
    }

    private boolean work = false;

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private long startTime = 0l;

    @SuppressLint("WrongConstant")
    @Override
    public void run() {

        mediaCodec.start();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        byte[] temp;
        work = true;
        int outputBufferIndex = -1;
        Bundle params;
        //Us 微秒
        long lastTime = 0l;
        //Us
        long now = 0l;
        //work了多长的时间 s
        long workTime = 0l;

        while (work) {

            now = System.currentTimeMillis();
            //每隔2s手动出发生成一个I帧
            if (now - lastTime > 2000) {
                l.i("手动出发生成一个I帧");
                params = new Bundle();
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                mediaCodec.setParameters(params);
                //记录上次生成I帧的时间
                lastTime = System.currentTimeMillis();
            }

            //微秒
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100);

            if (outputBufferIndex >= 0) {

                if (startTime == 0) {
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }

                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex);
                temp = new byte[outputBuffer.remaining()];

                //处理编码好的buffer
                workTime = bufferInfo.presentationTimeUs / 1000 - startTime;

                outputBuffer.get(temp);
                //FileUtil.writeBytesTo16Chars(temp, "rtmp_projection.txt");

                queueEncodeBuffer(temp, workTime);

                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            }
        }
        myMediaProjection.stopMe();
    }

    private RTMPPacket rtmpPacket;

    private void queueEncodeBuffer(byte[] temp, long workTime) {

        rtmpPacket = new RTMPPacket(temp, temp.length, RTMPPacket.VIDEO_ENCODE_DATA, workTime);

        myMediaProjection.queueEncodeBuffer(rtmpPacket);

    }

}
