package com.zzm.play.rtmp;

import android.app.Activity;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.view.Surface;

import com.zzm.play.utils.l;

import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyMediaProjection extends Thread {

    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private H26xEnCode h26xEnCode;
    private AudioEncode audioEncode;

    static {
        System.loadLibrary("rtmp_live");
    }

    public MyMediaProjection(MediaProjectionManager mediaProjectionManager) {
        this.mediaProjectionManager = mediaProjectionManager;
        h26xEnCode = new H26xEnCode();
        audioEncode = new AudioEncode();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {

            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);

            if (null != mediaProjection)
                startProjection();
        }

    }

    private MediaCodec mediaCodec;
    private VirtualDisplay virtualDisplay;
    private int width = 720;
    private int height = 1280;

    private boolean projection = false;

    private void startProjection() {

        try {

            //h264 rtmp目前支持
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //直播超清分辨率
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            //码率
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            //帧率 直播可以接受较低的帧率 但是短视频不行
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            //生成I帧间隔时间 2s生成一个I帧
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            //buffer从哪里得到
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //配置编码器
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            Surface inputSurface = mediaCodec.createInputSurface();
            //把编码器的surface给了projection去装载录屏数据，然后就可以到编码器去拿取编码好的数据
            virtualDisplay = mediaProjection.createVirtualDisplay("projection", width, height, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, inputSurface, null, null);

            //开始从队列里面取buffer 然后发送buffer给服务器
            start();

        } catch (Exception e) {
            l.i(e.toString());
            e.printStackTrace();
        }

    }

    private BlockingQueue<RTMPPacket> queue = new LinkedBlockingQueue<>();

    public void queueEncodeBuffer(RTMPPacket rtmpPacket) {

        if (!projection) return;

        queue.add(rtmpPacket);

    }

    private String url = "rtmp://192.168.71.129/mingzz_live/xxx";

    @Override
    public void run() {

        projection = true;

        RTMPPacket rtmpPacket;

        //rtmp连接服务器
        boolean connect = connect(url);
        l.i("rtmp connect result : " + connect);
        if (!connect) return;

        //视频编码
        h26xEnCode.startEncode(mediaCodec, this);

        //录音音频编码
        audioEncode.startEncode(this);

        while (projection) {

            try {

                rtmpPacket = queue.take();

                l.i("发送数据给服务器，data length : " + rtmpPacket.getDataLength());
                sendData(rtmpPacket.getData(),
                        rtmpPacket.getDataLength(),
                        rtmpPacket.getTime(),
                        rtmpPacket.getDataType());


            } catch (Exception e) {
                l.i(e.toString());
                e.printStackTrace();
            }

        }

    }

    public native boolean connect(String url);

    public native void sendData(byte[] data, int dataLength, long time, int type);

    public void stopMe() {
        l.i("projection stopMe");
        projection = false;
        if (null != mediaProjection) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        if (null != virtualDisplay) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }

        if (null != h26xEnCode) {
            h26xEnCode.setWork(false);
            h26xEnCode.setStartTime(0);
        }

        if (null != audioEncode) {
            audioEncode.destroyMe();
            audioEncode = null;
        }
    }
}
