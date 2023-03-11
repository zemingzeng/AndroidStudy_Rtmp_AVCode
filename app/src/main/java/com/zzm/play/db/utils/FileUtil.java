package com.zzm.play.db.utils;

import com.zzm.play.utils.l;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtil {

    public static void CopySingleFile(String oldPathFile, String newPathFile) {
        try {
            int byteread = 0;
            File oldfile = new File(oldPathFile);
            File newFile = new File(newPathFile);
            File parentFile = newFile.getParentFile();
            if (null != parentFile)
                if (!parentFile.exists()) {
                    if (parentFile.mkdirs())
                        l.i("FileUtil 创建文件夹成功：" + parentFile.getAbsolutePath());
                }
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPathFile); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPathFile);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            l.i(e.toString());
        }
    }

}
