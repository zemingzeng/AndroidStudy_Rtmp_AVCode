package com.zzm.play.x264;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;

import com.zzm.play.utils.l;

public class AudioRecorderTool {

    //采样率
    private int sampleRate;
    //通道数
    private int channelConfig;
    //最小的多大的数据大小来装载pcm
    private int minBufferSize;
    private AudioRecord audioRecord;
    //录音在子线程
    private HandlerThread handlerThread;
    private Handler handler;

    public AudioRecorderTool(int sampleRate, int channelCount) {

        this.sampleRate = sampleRate;

        channelConfig = channelCount == 2 ? AudioFormat.CHANNEL_IN_STEREO :
                AudioFormat.CHANNEL_IN_MONO;

        //返回-2 硬件则不支持此参数
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfig,
                AudioFormat.ENCODING_PCM_16BIT);
        l.i("AudioRecord getMinBufferSize minBufferSize ： " + minBufferSize);

        handlerThread = new HandlerThread("audio-record");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        //init faac
        nativeFaacEncodeInit();

        //打开faac
        int faacEncodeInputBufferSize = nativeFaacEncodeOpen(sampleRate, channelCount);
        pcmData = new byte[faacEncodeInputBufferSize];
        l.i("faac  get input BufferSize ： " + faacEncodeInputBufferSize);
        //一个是硬件根据最小采样数目来算出minBufferSize，一个是编码器那边根据参数算出的输入的buffer大小
        //为了不出问题所以用最大的buffer size来装载pcm数据
        minBufferSize = Math.max(minBufferSize, faacEncodeInputBufferSize);

    }

    private byte[] pcmData;

    public void startRecording() {

        handler.post(() -> {

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            int readDataSize = -1;

            audioRecord.startRecording();

            while (null != audioRecord
                    && audioRecord.getRecordingState() ==
                    AudioRecord.RECORDSTATE_RECORDING) {

                //发送数据给faac去编码
                readDataSize = audioRecord.read(pcmData, 0, pcmData.length);
                l.i("audio recorder record pcm data size : " + readDataSize);

                if (readDataSize > 0) {

                    sendAudioPcmData(pcmData, readDataSize/2);

                }
            }

        });

    }


    public void releaseMe() {

        if (null != handlerThread) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        if (null != handler) {
            handler = null;
        }

        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }

    }

    //初始化
    private native void nativeFaacEncodeInit();

    private native int nativeFaacEncodeOpen(int sampleRate, int channelCount);

    private native void sendAudioPcmData(byte[] pcmData, int dataLength);
}
