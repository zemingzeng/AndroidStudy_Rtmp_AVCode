package com.zzm.router;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.zzm.router.util.AppUtil;
import com.zzm.router.util.l;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {

    private Map<String, String> routerActivityMap;
    private Application c;

    private Router() {
    }


    public static class SingleTonHolder {
        private static final Router router = new Router();

        private SingleTonHolder() {
        }

        public static Router getInstance() {
            return router;
        }
    }

    public void init(Application c) {
        this.c = c;

        //调用生成的router activity util把router注解了的activity添加到map中
        List<String> list = AppUtil.getClassFilesByPackageName("com.zzm", c);
        for (String fullClassName : list) {
            //l.i("fullClassName:" + fullClassName);
            if (fullClassName.startsWith("com.zzm.annotation_process.")) {
                l.i("com.zzm.annotation_process包下的类：" + fullClassName);
                try {
                    Class<?> aClass = Class.forName(fullClassName);
                    //是不是IRouter子类
                    if (IRouter.class.isAssignableFrom(aClass)) {
                        l.i("IRouter子类：" + aClass.getCanonicalName());
                        ((IRouter) aClass.newInstance()).putRouterActivity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    l.i(e.toString());
                }
            }
        }
    }

    public void putRouterActivity(String tag, String fullClassName) {
        l.i("putRouterActivity tag:" + tag);
        if (null == routerActivityMap)
            routerActivityMap = new HashMap<>();
        routerActivityMap.put(tag, fullClassName);
    }

    public void jumpActivity(String tag) {
        String fullClassName;
//        l.i("jumpActivity map size:" + routerActivityMap.size());
        if (null != routerActivityMap && null != (fullClassName = routerActivityMap.get(tag))) {
            l.i("jumpActivity :" + fullClassName);
            Intent intent = new Intent();
            intent.setClassName(c, fullClassName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            c.startActivity(intent);
        }
    }

}
