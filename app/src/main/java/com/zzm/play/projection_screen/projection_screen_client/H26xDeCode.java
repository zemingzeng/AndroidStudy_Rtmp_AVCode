package com.zzm.play.projection_screen.projection_screen_client;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import com.zzm.play.projection_screen.projection_screen_server.H26xEnCode;
import com.zzm.play.utils.l;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class H26xDeCode implements Client.DataCallBack {

    private MediaCodec mediaCodec;
    private Client client;
    private Surface surface;

    public H26xDeCode(Surface surface) {
        init(surface);
    }

    private static final String URI = "ws://192.168.1.8:9876";

    private static final String ENCODE_TYPE = H26xEnCode.H265;

    private void init(Surface surface) {

        try {

            //初始化客户端
            URI uri = new URI(URI);
            client = new Client(uri);
            client.setDataCallBack(this);

            //MediaCodec
            mediaCodec = MediaCodec.createDecoderByType(ENCODE_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(ENCODE_TYPE,
                    H26xEnCode.width, H26xEnCode.height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, H26xEnCode.width * H26xEnCode.height);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            mediaCodec.configure(mediaFormat, surface, null, 0);

        } catch (Exception e) {
            l.i(e.toString());
        }

    }

    public void startMe() {

        mediaCodec.start();

        client.startMe();

    }


    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    @Override
    public void getData(byte[] bytes) {
        l.i("getData Bytes: " + Arrays.toString(bytes));
        l.i("decode : " + ENCODE_TYPE);

        //解码
        int index = mediaCodec.dequeueInputBuffer(10000);
        //l.i("dequeueInputBuffer index: " + index);
        if (index >= 0) {
            ByteBuffer buffer = mediaCodec.getInputBuffer(index);
            buffer.clear();
            buffer.put(bytes, 0, bytes.length);
            mediaCodec.queueInputBuffer(index, 0, bytes.length, System.currentTimeMillis()
                    , 0);
        }

        //get data
        int index1 = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
        while (index1 >= 0) {
            mediaCodec.releaseOutputBuffer(index1, true);
            index1 = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }

    }

    public void closeMe() {

        if (null != client)
            client.closeMe();

        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
        }

    }

}
