package com.zzm.play.utils;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtil {
    public static boolean checkPermission(Activity a) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && a.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            a.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            }, 1);
        }
        return false;
    }
}
