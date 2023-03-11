package com.zzm.play.camera2_h26x;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.zzm.play.utils.FileUtil;
import com.zzm.play.utils.l;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class H26xEnCode {

    public static final String H264 = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static final String H265 = MediaFormat.MIMETYPE_VIDEO_HEVC;

    //编码类型
    private String encodeType = H264;

    //帧宽高
    private int width;
    private int height;

    private Server server;

    public H26xEnCode(String encodeType, int width, int height, Server server) {
        this.encodeType = encodeType;
        this.width = width;
        this.height = height;
        this.server = server;

        //初始化media codec
        init();
    }

    private MediaCodec mediaCodec;

    private void init() {

        try {

            mediaCodec = MediaCodec.createEncoderByType(encodeType);

            MediaFormat mediaFormat = MediaFormat.createVideoFormat(encodeType, height, width);

            //格式
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            //码率 越高细节越多，一般宽*高即可
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, height * width);
            //帧率
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            //编码I帧时间间隔
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);

            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mediaCodec.start();

        } catch (IOException e) {
            l.i(e.toString());
        }

    }


    private byte[] temp;
    private int frameIndex;
    private long presentationTimeUs;
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

    public void startEncode(byte[] encodeBytes) {
        // l.i("startEncode");

        int inputIndex = mediaCodec.dequeueInputBuffer(100000);

        if (inputIndex >= 0) {

            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputIndex);

            inputBuffer.clear();

            inputBuffer.put(encodeBytes);

            presentationTimeUs = computePresentationTime(frameIndex);

            mediaCodec.queueInputBuffer(inputIndex, 0, encodeBytes.length, presentationTimeUs, 0);

            frameIndex++;

        }

        int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);

        while (outputIndex >= 0) {

            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputIndex);


            pushEncodedBytes(outputBuffer, bufferInfo);

            //---------test------------------
            //temp = new byte[outputBuffer.remaining()];
            //outputBuffer.get(temp);
            //l.i("encode : " + Arrays.toString(temp));
            //FileUtil.writeBytesTo16Chars(temp, "camera2.txt");
            //FileUtil.writeEncodeBytes(temp, "camera2.h26x");
            //---------test------------------

            mediaCodec.releaseOutputBuffer(outputIndex, false);

            //timeout---->0
            outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }

    }

    private void pushEncodedBytes(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo) {

        if (server == null)
            return;

        boolean isConnected = server.isOpen();
        l.i("pushEncodedBytes isConnected : " + isConnected);

        temp = dealEncodedBuffer(outputBuffer, bufferInfo, false, encodeType);

        //if (!isConnected)
        //return;


        //发送数据
        //l.i("pushEncodedBytes : " + Arrays.toString(temp));
        server.sendData(temp);

    }


    //I frame
    private static final byte H265_KEY_FRAME_TYPE = 19;
    //vps=32 sps=33 pps=34 p_frame=1 b_frame=0
    private static final byte H265_KEY_CONFIG_TYPE = 32;
    private static final byte H264_KEY_FRAME_TYPE = 5;
    private static final byte H264_KEY_CONFIG_TYPE = 7;
    private byte[] key_config_bytes;

    private byte[] dealEncodedBuffer(ByteBuffer outputBuffer, MediaCodec.BufferInfo bufferInfo, boolean record, String encodeType) {

        l.i("encode : " + encodeType);

        byte[] bytes = new byte[bufferInfo.size];
        outputBuffer.get(bytes);

        l.i("encode bytes : " + Arrays.toString(bytes));

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
            l.i("type : " + type);

            if (type == H265_KEY_CONFIG_TYPE) {
                key_config_bytes = bytes;
            } else if (type == H265_KEY_FRAME_TYPE) {
                temp = bytes;
                bytes = new byte[key_config_bytes.length + bufferInfo.size];
                System.arraycopy(key_config_bytes, 0, bytes, 0, key_config_bytes.length);
                System.arraycopy(temp, 0, bytes, key_config_bytes.length, temp.length);
            }

        }

        return bytes;
    }


    private long computePresentationTime(long frameIndex) {
        return 200 + frameIndex * 1000000 / 29;
    }

    public void closeMe() {
        if (null != mediaCodec) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
    }
}
