package com.zzm.play.utils;

/**
 * 两大类，总共4小类
 * YUV420{p:yv12 yu12(I420) sp:nv12 nv21}
 */
public class  YUVUtil {

    //yyy..  uv uv uv  YUV420sp
    public static final int NV12 = 1;

    //yyy..  vu vu vu  YUV420sp
    public static final int NV21 = 2;

    //yyy.. uuu vvv I420 YU12
    public static final int YUV420p = 3;

    //yyy.. vvv uuu YV12
    public static final int YV12 = 4;

    public static byte[] YUV420BytesClockwise90Rotate(byte[] bytes, int width, int height, int yuv_type) {

        int totalSize = width * height * 3 / 2;
        int ySize = width * height;
        byte[] temp = new byte[totalSize];

        //Y rotate
        int index = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                temp[index++] = bytes[i + width * (height - j - 1)];
            }
        }

        //u v rotate
        if (yuv_type == YUVUtil.NV12) {
            //sp类型 uv看成整体
            int uvHeight = height / 2;
            int uvWidth = width / 2;
            int uvStartIndex;
            for (int i = 0; i < uvWidth; i++) {
                for (int j = 0; j < uvHeight; j++) {

                    uvStartIndex = (i + uvWidth * (uvHeight - j - 1)) * 2 + ySize;
                    temp[index++] = bytes[uvStartIndex];
                    temp[index++] = bytes[uvStartIndex + 1];

                }
            }
        } else if (yuv_type == YUVUtil.YUV420p) {

            int uvCount = ySize / 4;
            int w = width / 2;
            int h = height / 2;
            int index1 = ySize + uvCount;
            int uvPosition;
            for (int i = 0; i < w; i++) {
                for (int k = 0; k < h; k++) {
                    uvPosition = w * (h - 1) + i - k * w;
                    //u
                    temp[index++] = bytes[ySize + uvPosition];
                    //v
                    temp[index1++] = bytes[ySize + uvCount + uvPosition];
                }
            }

        }

        return temp;
    }


}
