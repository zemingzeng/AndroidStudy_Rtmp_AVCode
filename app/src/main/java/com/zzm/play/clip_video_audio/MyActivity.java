package com.zzm.play.clip_video_audio;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.utils.FileUtil;
import com.zzm.play.utils.l;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.clip_video_audio_layout);

        init();

        doSomething();

    }

    private void init() {

    }

    private final String CLIP_TYPE_VIDEO = "video/";
    private final String CLIP_TYPE_AUDIO = "audio/";

    private void doSomething() {
        //TaylorSwift.mp4
        String mediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "TaylorSwift.mp4";
        l.i("mediaPath :" + mediaPath + "  " + new File(mediaPath).exists());
        String outputMediaPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "TaylorSwift.h264";
        int statTime = 0 * 1000 * 1000;
        int endTime = statTime + 10 * 1000 * 1000;
        new Thread() {
            @Override
            public void run() {
                super.run();
                clipAudioOrVideo(mediaPath, CLIP_TYPE_VIDEO, statTime, endTime, outputMediaPath);
            }
        }.start();
    }

    /**
     * @param inputMediaPath 文件路径
     * @param startTime      us 微秒
     * @param endTime        us 微秒
     */
    private void clipAudioOrVideo(String inputMediaPath, String clipType, int startTime, int endTime, String outputMediaPath) {

        if (endTime < startTime)
            return;

        //相当于解压工具
        MediaExtractor mediaExtractor = new MediaExtractor();

        try {

            mediaExtractor.setDataSource(inputMediaPath);

            //选择轨道去解析，一般基本包含音频轨道和视频轨道
            int trackIndex = selectTrack(clipType, mediaExtractor);
            if (trackIndex < 0)
                return;

            //设置将要处理的轨道信息
            mediaExtractor.selectTrack(trackIndex);
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(trackIndex);
//            if (null != mediaFormat.getByteBuffer("csd-0")) {
//                ByteBuffer byteBuffer = mediaFormat.getByteBuffer("csd-0");
//                byte[] bytes = new byte[byteBuffer.remaining()];
//                l.i("-------csd-0---------"+Arrays.toString(bytes));
//            }
//            if (null != mediaFormat.getByteBuffer("csd-1")) {
//                ByteBuffer byteBuffer = mediaFormat.getByteBuffer("csd-1");
//                byte[] bytes = new byte[byteBuffer.remaining()];
//                l.i("-------csd-1---------"+Arrays.toString(bytes));
//            }
            String mimeType = mediaFormat.getString(MediaFormat.KEY_MIME);
            int maxBufferSize = 100 * 1000;
            //获取支持的最大的buffer size,若取不到就用默认的
            if (mediaFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE))
                maxBufferSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);

            //seek to the sync sample closest to the specified time
//            mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            //media codec解码器根据获取到的 mime 类型初始化以及配置
            MediaCodec mediaCodec = MediaCodec.createDecoderByType(mimeType);
            mediaCodec.configure(mediaFormat, null, null, 0);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            //一、创建Buffer对象的方式？
            //
            //1、从JVM堆中分配内存，
            //
            //2、也可以OS本地内存中分配，由于本地缓冲区避免了缓冲区复制，在性能上相对堆缓冲区有一定优势，但同时也存在一些弊端。
            //二、两种缓冲区对应的API如下：
            //
            //1、JVM堆缓冲区：ByteBuffer.allocate(size)
            //
            //2、本地缓冲区：ByteBuffer.allocateDirect(size)//属于系统级的内存开销，就是操作系统直接分配的
            // ByteBuffer byteBuffer = ByteBuffer.allocate(maxBufferSize) jvm分配内存
            ByteBuffer maxBuffer = ByteBuffer.allocateDirect(maxBufferSize);
            byte[] temp = null;
            mediaCodec.start();

            //Channel是对I/O操作的封装。
            // FileChannel配合着ByteBuffer，
            // 将读写的数据缓存到内存中，然后以
            // 批量/缓存的方式read/write，
            // 省去了非批量操作时的重复中间操作，
            // 操纵大文件时可以显著提高效率
            FileChannel fileChannel = new FileOutputStream(new File(outputMediaPath),
                    true)
                    .getChannel();

            boolean getData = false;

            while (true) {
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(100000);

                //获取到可编码buffer采取解码
                if (inputBufferIndex >= 0) {

                    long sampleTime = -1;
                    getData = false;

                    while (!getData) {

                        sampleTime = mediaExtractor.getSampleTime();
                        l.i("sample time : " + sampleTime);

                        if (sampleTime == -1) {
                            break;
                        } else if (sampleTime < startTime) {
                            //取下一个sample data

                            //---------test-------------
                            //去获取压缩数据 并且设置bufferInfo信息
//                            bufferInfo.size = mediaExtractor.readSampleData(maxBuffer, 0);
//                            //把 实际的remaining 压缩数据给到dsp解码
//                            temp = new byte[maxBuffer.remaining()];
//                            maxBuffer.get(temp);
//                            if (null != temp && temp.length > 4) {
//                                if (temp[0] == 0x00 &&
//                                        temp[1] == 0x00 &&
//                                        temp[2] == 0x00 &&
//                                        temp[3] == 0x01 && temp[4] == 0x65) {
//                                    l.i("-------sps------------");
//                                }
//                            }


                            mediaExtractor.advance();
                            continue;
                        }
                        getData = true;
                    }

                    if (sampleTime > endTime)
                        break;

                    maxBuffer.clear();
                    //去获取压缩数据 并且设置bufferInfo信息
                    bufferInfo.size = mediaExtractor.readSampleData(maxBuffer, 0);
                    bufferInfo.presentationTimeUs = sampleTime;
                    bufferInfo.flags = mediaExtractor.getSampleFlags();
                    //l.i("getSampleFlags()---" + bufferInfo.flags);

                    if (clipType.equals(CLIP_TYPE_AUDIO)) {
                        int chanelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                        l.i("音频通道数：" + chanelCount);
                    }

                    //记录剪辑好的视频数据
                    //l.i("maxBuffer.remaining()---" + maxBuffer.remaining());
                    //if (clipType.equals(CLIP_TYPE_VIDEO))
                    //fileChannel.write(maxBuffer, maxBuffer.remaining());

                    //把 实际的remaining 压缩数据给到dsp解码
                    temp = new byte[maxBuffer.remaining()];
                    maxBuffer.get(temp);

//                    if (null != temp && temp.length > 4) {
//                        if (temp[0] == 0x00 &&
//                                temp[1] == 0x00 &&
//                                temp[2] == 0x00 &&
//                                temp[3] == 0x01 && temp[4] == 0x67) {
//                            l.i("-------sps------------");
//                        }
//                    }

                    //l.i("---------------------" + Arrays.toString(temp));
                    //可以把数据记录到本地
                    //FileUtil.writeBytesTo16Chars(temp, "out_aac.txt");

                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                    inputBuffer.put(temp);
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, bufferInfo.size
                            , bufferInfo.presentationTimeUs, bufferInfo.flags);
                    mediaExtractor.advance();
                }

                //获取解码好的数据
                int outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);
                while (outBufferIndex >= 0) {

                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outBufferIndex);

                    //操作解码好的数据
                    if (clipType.equals(CLIP_TYPE_AUDIO))
                        fileChannel.write(outputBuffer);

                    //释放资源
                    mediaCodec.releaseOutputBuffer(outBufferIndex, false);

                    //直到把上一次给到的数据解码完
                    outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);

                }
            }
            l.i("处理完毕！！！！！");
            //关闭资源
            fileChannel.close();
            mediaExtractor.release();
            mediaCodec.stop();
            mediaCodec.release();

        } catch (IOException e) {
            l.i(e.toString());
        }
    }

    private int selectTrack(String mimeType, MediaExtractor mediaExtractor) {

        if (null == mediaExtractor)
            return -1;

        int trackCount = mediaExtractor.getTrackCount();

        for (int i = 0; i < trackCount; i++) {

            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            //MIME type 文档类型 ; 文件类型
            String mimeType_ = trackFormat.getString(MediaFormat.KEY_MIME);
            l.i("track index : " + i + "----mime type : " + mimeType_);

            if (mimeType_.contains(mimeType))
                return i;

        }

        return -1;
    }
}
