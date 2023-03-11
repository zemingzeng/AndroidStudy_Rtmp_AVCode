package com.zzm.router.util;

import android.app.Application;
import android.os.MessageQueue;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

public class AppUtil {

    /**
     * @param packageName 想要找apk中所有包含此包名的类
     * @param c           application
     * @return 想要找apk中所有包含此包名的类的list集合
     */
    public static List<String> getClassFilesByPackageName(String packageName, Application c) {
        List<String> list = new ArrayList<>();
        String path;
        MessageQueue sa;

        try {
            //path:base.apk在手机中的存储位置
            //c.getPackageName()是build.gradle中applicationId
            path = c.getPackageManager().getApplicationInfo(c.getPackageName(), 0).sourceDir;
            DexFile dexFile = new DexFile(path);
            Enumeration<String> enumeration = dexFile.entries();
            while (enumeration.hasMoreElements()) {
                //包名+类名
                String name = enumeration.nextElement();
                if (name.startsWith(packageName)) {
                    list.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            l.i(e.toString());
        }
        return list;
    }

}
