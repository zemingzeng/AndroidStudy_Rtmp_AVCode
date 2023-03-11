package com.zzm.play.video_mix;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import com.zzm.play.utils.l;

import java.nio.ByteBuffer;

public class VideoProcess extends Thread {

    private final String inputVideoPath1;
    private final String inputVideoPath2;
    private final String outPath;

    public VideoProcess(String inputVideoPath1, String inputVideoPath2, String outPath) {

        this.inputVideoPath1 = inputVideoPath1;

        this.inputVideoPath2 = inputVideoPath2;

        this.outPath = outPath;
    }


    @Override
    public void run() {

        videoMix(outPath, inputVideoPath1, inputVideoPath2);

    }

    private MediaMuxer mediaMuxer;
    private MediaExtractor mediaExtractor1;
    private MediaExtractor mediaExtractor2;
    private int muxerVideoTractIndex = -1;
    private int muxerVideoTractIndex1 = -1;
    private int muxerAudioTractIndex = -1;

    public void videoMix(String outPath, String inputVideoPath1, String inputVideoPath2) {

        try {

            mediaExtractor1 = new MediaExtractor();
            mediaExtractor1.setDataSource(inputVideoPath1);

            mediaExtractor2 = new MediaExtractor();
            mediaExtractor2.setDataSource(inputVideoPath2);

            //合层MP4
            mediaMuxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            //选取视频的音视频轨道
            int video1VideoTrackIndex = selectTrack(mediaExtractor1, false);
            int video1AudioTrackIndex = selectTrack(mediaExtractor1, true);
            int video2VideoTrackIndex = selectTrack(mediaExtractor2, false);
            int video2AudioTrackIndex = selectTrack(mediaExtractor2, true);

            //获取video1音频的总时长
            long duration1 = mediaExtractor1.getTrackFormat(video1AudioTrackIndex).getLong(MediaFormat.KEY_DURATION);
            l.i("audio1Duration : " + duration1);


            //获取video1视频的总时长
            long duration = mediaExtractor1.getTrackFormat(video1VideoTrackIndex).getLong(MediaFormat.KEY_DURATION);
            l.i("video1Duration : " + duration);

            //申请500kb内存来装载读取的数据
            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);
            //记录读取到的数据的信息
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            //读取到的数据大小，若为-1 则代表没有数据可以读取
            int sampleDataSize = -1;

            //开启muxer
            mediaMuxer.start();

            //往视频轨道里面填充读取到的video1的视频数据
            mediaExtractor1.selectTrack(video1VideoTrackIndex);
            info.presentationTimeUs = 0;
            while ((sampleDataSize = mediaExtractor1.readSampleData(byteBuffer, 0)) > 0) {

                info.size = sampleDataSize;
                info.offset = 0;
                info.flags = mediaExtractor1.getSampleFlags();
                info.presentationTimeUs = mediaExtractor1.getSampleTime();
                mediaMuxer.writeSampleData(muxerVideoTractIndex, byteBuffer, info);
                mediaExtractor1.advance();
            }
            l.i("往视频轨道里面填充读取到的video1的视频数据over");

            //往音频轨道里面填充读取到的video1的音频数据
            mediaExtractor1.unselectTrack(video1VideoTrackIndex);
            mediaExtractor1.selectTrack(video1AudioTrackIndex);
            byteBuffer = ByteBuffer.allocate(500 * 1024);
            info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            while ((sampleDataSize = mediaExtractor1.readSampleData(byteBuffer, 0)) > 0) {

                info.size = sampleDataSize;
                info.offset = 0;
                info.flags = mediaExtractor1.getSampleFlags();
                info.presentationTimeUs = mediaExtractor1.getSampleTime();
                mediaMuxer.writeSampleData(muxerAudioTractIndex, byteBuffer, info);
                mediaExtractor1.advance();
            }
            l.i("往音频轨道里面填充读取到的video1的音频数据over");

            //往视频轨道里面填充读取到的video2的视频数据
            mediaExtractor2.selectTrack(video2VideoTrackIndex);
            byteBuffer = ByteBuffer.allocate(500 * 1024);
            info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            while ((sampleDataSize = mediaExtractor2.readSampleData(byteBuffer, 0)) > 0) {
                info.size = sampleDataSize;
                info.offset = 0;
                info.flags = mediaExtractor2.getSampleFlags();
                info.presentationTimeUs = mediaExtractor2.getSampleTime() + duration;
                mediaMuxer.writeSampleData(muxerVideoTractIndex, byteBuffer, info);
                mediaExtractor2.advance();
            }
            l.i("往视频轨道里面填充读取到的video2的视频数据over");


            //往音频轨道里面填充读取到的video2的音频数据
            mediaExtractor2.unselectTrack(video2VideoTrackIndex);
            mediaExtractor2.selectTrack(video2AudioTrackIndex);
            byteBuffer = ByteBuffer.allocate(500 * 1024);
            info = new MediaCodec.BufferInfo();
            info.presentationTimeUs = 0;
            while ((sampleDataSize = mediaExtractor2.readSampleData(byteBuffer, 0)) > 0) {
                info.size = sampleDataSize;
                info.offset = 0;
                info.flags = mediaExtractor2.getSampleFlags();
                info.presentationTimeUs = mediaExtractor2.getSampleTime() + duration1;
                mediaMuxer.writeSampleData(muxerAudioTractIndex, byteBuffer, info);
                mediaExtractor2.advance();
            }
            l.i("往音频轨道里面填充读取到的video2的音频数据over");

        } catch (Exception e) {

            l.i(e.toString());

        } finally {

            mediaExtractor1.release();
            mediaExtractor2.release();
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaExtractor1 = null;
            mediaExtractor2 = null;
            mediaMuxer = null;

        }

    }

    private int selectTrack(MediaExtractor mediaExtractor, boolean isAudio) {

        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String type = trackFormat.getString(MediaFormat.KEY_MIME);
            if (type.startsWith("video/") && !isAudio) {

                //添加视频轨道
                if (muxerVideoTractIndex == -1) {
                    muxerVideoTractIndex = mediaMuxer.addTrack(trackFormat);
                    l.i("添加视频轨道");
                }else {
                    //muxerVideoTractIndex1=mediaMuxer.addTrack(trackFormat);
                }
                return i;

            } else if (type.startsWith("audio/") && isAudio) {

                //添加音频轨道
                if (muxerAudioTractIndex == -1) {
                    muxerAudioTractIndex = mediaMuxer.addTrack(trackFormat);
                    l.i("添加音频轨道");
                }

                return i;

            }
        }
        return -1;
    }
}
