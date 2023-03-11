package com.zzm.play.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtil {

    //编码好的字节数据写到根目录
    public static void writeEncodeBytes(byte[] bytes, String name) {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(
                    Environment.getExternalStorageDirectory() + File.separator + name,
                    true);
            fileOutputStream.write(bytes);
            fileOutputStream.write('\n');
        } catch (Exception e) {
            l.i(e.toString());
        } finally {
            try {
                if (null != fileOutputStream)
                    fileOutputStream.close();
            } catch (IOException e) {
                l.i(e.toString());
            }
        }
    }

    //编码好的数据以16进制字符方式写到文件中
    public static void writeBytesTo16Chars(byte[] bytes, String name) {
        char[] HEX_CHAR_TABLE = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(HEX_CHAR_TABLE[(b & 0xf0) >> 4]);
            stringBuilder.append(HEX_CHAR_TABLE[b & 0x0f]);
        }
        l.i("写入的16进制数据：" + stringBuilder.toString());

        FileWriter fileWriter = null;

        try {
            fileWriter = new FileWriter(
                    Environment.getExternalStorageDirectory() + File.separator + name,
                    true);
            fileWriter.write(stringBuilder.toString());
            fileWriter.write("\n");
        } catch (IOException e) {
            l.i(e.toString());
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                l.i(e.toString());
            }
        }
//        return stringBuilder.toString();
    }
}


