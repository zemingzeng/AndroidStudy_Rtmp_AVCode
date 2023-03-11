package com.zzm.play.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import com.zzm.play.plugin_test.ThirdAppManager;
import com.zzm.play.utils.AppUtil;
import com.zzm.play.utils.l;
import com.zzm.router.Router;

import java.util.List;

public class MyApplication extends Application {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {

        int rotation = getSystemService(WindowManager.class).getDefaultDisplay().getRotation();
        l.i("MyApplication rotation : " + rotation);

        super.onCreate();

        //创建数据库
        //DbManager.getInstance().init(getApplicationContext(), "zemingzeng.db");

        l.i("MyApplication onCreate getBaseContext:" + getBaseContext().toString());
        try {
            l.i(" this.getPackageName():" + this.getPackageName());
            l.i(" getPackageManager().getApplicationInfo(this.getPackageName(), 0).sourceDir:" + getPackageManager().getApplicationInfo(this.getPackageName(), 0).sourceDir);
            List<String> classFilesByPackageName = AppUtil.getClassFilesByPackageName(getPackageName(), this);
            for (String classFullName : classFilesByPackageName) {
                // l.i("apk中包含" + getPackageName() + "的类有：" + classFullName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

//        //初始化路由
//        Router.SingleTonHolder.getInstance().init(this);
//getBaseContext()
//        getApplicationContext()
        //初始化plugin manager
        ThirdAppManager.init(getCacheDir().getAbsolutePath() + "/third_plugin-debug.apk", this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        l.i("MyApplication attachBaseContext : " + base.toString());
        super.attachBaseContext(base);
    }
}
