package com.zzm.play.projection_screen.projection_screen_server;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.view.Surface;

import com.zzm.play.utils.FileUtil;
import com.zzm.play.utils.l;

import java.nio.ByteBuffer;
import java.util.Arrays;


public class H26xEnCode extends Thread {

    private MediaCodec mediaCodec;
    private Server server;
    private final MediaProjection mediaProjection;

    public H26xEnCode(MediaProjection mediaProjection, Server server) {
        this.mediaProjection = mediaProjection;
        this.server = server;
        if (null != mediaProjection)
            init();
    }

    public static final String H264 = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static final String H265 = MediaFormat.MIMETYPE_VIDEO_HEVC;
    public static int width = 1080;
    public static int height = 1920;

    private void init() {
        try {

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(ENCODE_TYPE, width, height);
            //颜色格式，表示要渲染到surface view
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            //码率 越高细节越多
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height);
            //帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
            //编码I帧时间间隔
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mediaCodec = MediaCodec.createEncoderByType(ENCODE_TYPE);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

        } catch (Exception e) {
            l.i(e.toString());
        }
    }


    private VirtualDisplay virtualDisplay;

    public void startProjectionScreenAndEncode() {
        Surface surface = mediaCodec.createInputSurface();
        //录屏 然后直接去dsp去encode后的buffer 但是没有B帧 底层应该没给接口
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "virtualDisplay", width, height, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
                , surface, null, null);
        start();
    }

    private boolean work = true;

    private final String ENCODE_TYPE = H265;

    @Override
    public void run() {

        mediaCodec.start();

        //记录编码后的buffer的信息
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        //循环去取编码好的数据
        while (work) {
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (index >= 0) {
                //拿到编码后的数据
                l.i("编码后的数据拿到了！！");
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(index);

                dealBuffer(outputBuffer, bufferInfo, false, ENCODE_TYPE);

                //释放buffer
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }

        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
        }
    }

    //I frame
    private static final byte H265_KEY_FRAME_TYPE = 19;
    //vps=32 sps=33 pps=34 p_frame=1 b_frame=0
    private static final byte H265_KEY_CONFIG_TYPE = 32;
    private static final byte H264_KEY_FRAME_TYPE = 5;
    private static final byte H264_KEY_CONFIG_TYPE = 7;
    private byte[] key_config_bytes;

    private void dealBuffer(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean record, String encodeType) {

        byte[] bytes = new byte[bufferInfo.size];
        outputBuffer.get(bytes);

        //记录buffer到本地
        if (record) {
            FileUtil.writeEncodeBytes(bytes, "encode_bytes.h26x");
            FileUtil.writeBytesTo16Chars(bytes, "encode_16chars.txt");
        }

        byte typeByteOffset = 4;
        if (outputBuffer.get(2) == 0x01)
            typeByteOffset = 3;
        byte typeByte = outputBuffer.get(typeByteOffset);

        byte[] temp = null;
        int type = -1;

        if (H264.equals(encodeType)) {
            type = typeByte & 0x1f;

            if (type == H264_KEY_CONFIG_TYPE) {

                key_config_bytes = bytes;

            } else if (type == H264_KEY_FRAME_TYPE) {

                temp = bytes;
                bytes = new byte[key_config_bytes.length + bytes.length];
                System.arraycopy(key_config_bytes, 0, bytes, 0, key_config_bytes.length);
                System.arraycopy(temp, 0, bytes, key_config_bytes.length, temp.length);

            }

        } else if (H265.equals(encodeType)) {

            type = (typeByte & 0x7e) >> 1;

            if (type == H265_KEY_CONFIG_TYPE) {
                key_config_bytes = bytes;
            } else if (type == H265_KEY_FRAME_TYPE) {
                temp = bytes;
                bytes = new byte[key_config_bytes.length + bufferInfo.size];
                System.arraycopy(key_config_bytes, 0, bytes, 0, key_config_bytes.length);
                System.arraycopy(temp, 0, bytes, key_config_bytes.length, temp.length);
            }

        }

        //推送编码好的数据
        if (server != null) {
            server.sendData(bytes);
            l.i("服务端发送的编码数据： " + "server open=" + server.isOpen() + "   " + Arrays.toString(bytes));
        }
    }

    public void stopMe() {
        work = false;
    }
}
