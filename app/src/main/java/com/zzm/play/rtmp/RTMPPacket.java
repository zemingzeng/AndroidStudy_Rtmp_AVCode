package com.zzm.play.rtmp;

public class RTMPPacket {

    public static final int VIDEO_ENCODE_DATA = 0;
    public static final int AUDIO_HEADER_DATA = 1;
    public static final int AUDIO_ENCODE_DATA = 2;
    private byte[] data;
    private int dataLength;

    public RTMPPacket(byte[] data, int dataLength, int dataType, long time) {
        this.data = data;
        this.dataLength = dataLength;
        this.dataType = dataType;
        this.time = time;
    }

    private int dataType;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    private long time;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }


}
