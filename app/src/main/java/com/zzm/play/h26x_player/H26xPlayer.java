package com.zzm.play.h26x_player;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.zzm.play.utils.l;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class H26xPlayer implements Runnable {
    private String path;
    private Surface surface;
    private MediaCodec mediaCodec;
    private Context c;
    private final String H264 = "video/avc";
    private final String H265 = "video/hevc";

    public H26xPlayer(String path, Surface surface, Context c) {
        this.path = path;
        this.surface = surface;
        this.c = c;
        try {
//            mediaCodec = MediaCodec.createDecoderByType(H264);
            mediaCodec = MediaCodec.createDecoderByType(H265);
//            int w = 368;
//            int h = 384;
            int w = 1280;
            int h = 672;
//            MediaFormat mediaFormat = MediaFormat.createVideoFormat(H264, w, h);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(H265, w, h);
            //帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
            //MediaCrypto:加密  flags:
            mediaCodec.configure(mediaFormat, surface, null, 0);
            l.i(path);
        } catch (Exception e) {
            l.i(e.toString());
        }
    }

    public void play() {
        l.i("video play");
        mediaCodec.start();
        new Thread(this).start();
    }

    @Override
    public void run() {
        l.i("run");
        try {
            decodeH26x();
        } catch (Exception e) {
            l.i(e.toString());
        }
    }

    private void decodeH26x() throws Exception {

        byte[] bytes = getBytes(path);

        //获取dsp中维护的输入ByteBuffers(16个左右),
        // 可以把你想要解码的数据装入进去送给dsp去解码
        //但是不是每个Buffer都可以使用
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();

        int startIndex = 0;
        int nextStartIndex;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {

            //用10000ns时间去找到dsp维护的可用的Buffer,没有则返回-1
            int index = mediaCodec.dequeueInputBuffer(10000);

            if (index >=0) {
                //可用的buffer
                ByteBuffer inputBuffer = inputBuffers[index];
                //用前先清空一下，以免有脏数据
                inputBuffer.clear();


                //去拿需要解码的帧压缩编码的数据
                nextStartIndex = findByFrame(bytes, startIndex+2, bytes.length);

                //放入需要解码的数据
                inputBuffer.put(bytes, startIndex, nextStartIndex - startIndex);
                //通知dsp芯片去解码
                mediaCodec.queueInputBuffer(index, 0, nextStartIndex - startIndex, 0, 0);


                startIndex = nextStartIndex;
            } else {
                continue;
            }

            //dsp送去解码的buffer 和 解码好的buffer不在同意队列里面 所以
            //10000ns 手动去查dsp解码好的数据,如果这段时间内解码好了就返回
            //解码好的buffer index
            // bufferInfo: Will be filled with buffer meta data.
            int outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (outIndex >= 0) {
                //手动释放buffer 并且渲染到之前给到surface ,如果不渲染则传false
                mediaCodec.releaseOutputBuffer(outIndex, true);
            } else {
                l.i("解码失败！");
            }
        }
    }



    private int findByFrame(byte[] bytes, int startIndex, int totalSize) {

        //h264 分隔符 0x 00 00 00 01 或者 0x 00 00 01
        for (int i = startIndex; i < totalSize; i++) {
            if (bytes[i] == 0x00 && bytes[i + 1] == 0x00) {
                if (bytes[i + 2] == 0x00 && bytes[i + 3] == 0x01) {
                    //分隔符 0x 00 00 00 01
                    return i ;
               } else if (bytes[i + 2] == 0x01) {
                    //分隔符 0x 00 00 01
                    return i;
                }
            }
        }
        return -1;
    }


    private byte[] getBytes(String path) throws Exception {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        int length;
        int size = 1024;
        byte[] bytes = new byte[size];
        try {
            inputStream = new DataInputStream(new FileInputStream(new File(path)));
            byteArrayOutputStream = new ByteArrayOutputStream();
            while ((length = inputStream.read(bytes, 0, size)) != -1)
                byteArrayOutputStream.write(bytes, 0, length);
            bytes = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            l.i(e.toString());
        } finally {
            if (null != byteArrayOutputStream)
                byteArrayOutputStream.close();
            if (null != inputStream)
                inputStream.close();
        }
        return bytes;
    }
}
