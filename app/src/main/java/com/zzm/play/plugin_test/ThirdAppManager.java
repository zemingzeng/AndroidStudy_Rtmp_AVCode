package com.zzm.play.plugin_test;

import android.content.Context;

import com.zzm.play.utils.l;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

public class ThirdAppManager {


    private ThirdAppManager() {
    }

    public static class SingletonHolder {
        private static final ThirdAppManager instance = new ThirdAppManager();

        private SingletonHolder() {
        }

        public static ThirdAppManager getInstance() {
            return instance;
        }
    }

    //通过反射加载外部apk中dex文件加载到内部，然后就可以使用外部apk中的类
    public static void init(String apkPath, Context c) {
        l.i("ThirdAppManager  init apkPath:" + apkPath);
        //反射主要是拿到BaseDexClassLoader下的
        // private final DexPathList pathList字段以及
        //DexPathList中的private final Element[] dexElements字段

        try {

            //本app的类加载器
            PathClassLoader pathClassLoader = ((PathClassLoader) c.getClassLoader());

            // private final DexPathList pathList字段获取
            Class<?> baseDexClassLoaderClass = Class.forName("dalvik.system.BaseDexClassLoader");
            Field pathListFiled = baseDexClassLoaderClass.getDeclaredField("pathList");
            pathListFiled.setAccessible(true);

            //private final Element[] dexElements字段
            Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
            Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);

            //解析apk后优化dex缓存目录
            String optimizedDirectory = c.getCacheDir().getAbsolutePath();
            l.i("PluginManager c.getCacheDir().getAbsolutePath():" + optimizedDirectory);
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath, optimizedDirectory, null, pathClassLoader);
            Object dexClassLoaderPathList = pathListFiled.get(dexClassLoader);
            Object[] dexClassLoaderPathListDexElements = ((Object[]) dexElementsField.get(dexClassLoaderPathList));

            //获取本app的类加载器中涉及的pathList字段以及dexElements字段
            Object pathClassLoaderPathList = pathListFiled.get(pathClassLoader);
            Object[] pathClassLoaderPathListDexElements = ((Object[]) dexElementsField.get(pathClassLoaderPathList));

            //然后把外部apk解析好的dex追加到本app类加载器中
            if (null != dexClassLoaderPathListDexElements && null != pathClassLoaderPathListDexElements) {

                Object[] o = (Object[]) Array.newInstance(dexElementsField.getType().getComponentType(), dexClassLoaderPathListDexElements.length +
                        pathClassLoaderPathListDexElements.length);
                System.arraycopy(pathClassLoaderPathListDexElements, 0, o, 0, pathClassLoaderPathListDexElements.length);
                System.arraycopy(dexClassLoaderPathListDexElements, 0, o, pathClassLoaderPathListDexElements.length, dexClassLoaderPathListDexElements.length);

                //最后更改本app类加载器的dexElementsField字段
                dexElementsField.set(pathClassLoaderPathList, o);
            }


        } catch (Exception e) {
            e.printStackTrace();
            l.i(e.toString());
        }

    }

}
