package com.zzm.play.test;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.LruCache;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dalvik.system.DexClassLoader;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//#!/bin/bash
//
//        export TOOLCHAIN=/zzm/Android/NDK/android-ndk-r21b/toolchains/llvm/prebuilt/linux-x86_64
//
//        export API=27
//
//        export ARM32OUT=armeabi-v7a
//
//        export ARM64OUT=arm64-v8a
//
//        #maybe modify
//        export OUT=$(dirname $(pwd))/out/$ARM64OUT
//        echo "out ----------------> $OUT"
//
//        export ARM32ACCOMPILER=armv7a-linux-androideabi$API-clang
//        export ARM32ACXXCOMPILER=armv7a-linux-androideabi$API-clang++
//
//        export ARM64CCOMPILER=aarch64-linux-android$API-clang
//        export ARM64CXXCOMPILER=aarch64-linux-android$API-clang++
//
//        #maybe modify
//        export CCOMPILER=$TOOLCHAIN/bin/$ARM64CCOMPILER
//        export CXXCOMPILER=$TOOLCHAIN/bin/$ARM64CXXCOMPILER
//        export CC=$CCOMPILER
//        export CXX=$CXXCOMPILER
//        echo "CC -----------------> $CC"
//        echo "CXX ----------------> $CXX"
//
//        #arm-linux-androideabi
//        export HOSTARM32=armv7a-linux-android
//        export HOSTARM64=aarch64-linux-android
//
//        #maybe modify
//        export HOST=$HOSTARM64
//        echo "HOST ----------------> $HOST"
//
//        export CROSS_PREFIX=$TOOLCHAIN/bin/arm-linux-androideabi-
//
//        function build
//        {
//        ./configure \
//        --disable-cli \
//        --prefix=$OUT \
//        --disable-cli \
//        --enable-shared  \
//        --enable-pic \
//        --host=$HOST \
//        --cross-prefix=$CROSS_PREFIX \
//        --sysroot=$TOOLCHAIN/sysroot \
//
//        make clean
//        make -j8
//        make install
//        }
//        build
class Temp extends AppCompatActivity {

    void play() {
        getApplication();
        getApplicationContext();
        getBaseContext();
        OkHttpClient okHttpClient = new OkHttpClient();

        Request request = new Request.Builder()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                ByteBuffer byteBuffer = ByteBuffer.allocate(100);
//                byteBuffer.clear();
//                byteBuffer.flip();
////                FileChannel channel = new FileInputStream("").getChannel();
////                channel.read()
////                Looper.getMainLooper()
//                View view;
//                view.setTag();
//                Glide.with(Temp.this).load("").into()
//                Handler
//                LruCache
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
            }
        });
    }

    void play1() {


//        DexClassLoader dexClassLoader=new DexClassLoader()
        Field field = null;
//        field.get()
    }

}


