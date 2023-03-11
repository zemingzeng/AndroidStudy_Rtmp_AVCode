package com.zzm.java_test;

import com.zzm.java_test.annotation.GET;
import com.zzm.java_test.annotation.IplayImp;
import com.zzm.java_test.annotation.Play;
import com.zzm.java_test.annotation.PlayImpl;
import com.zzm.java_test.proxy.IProxy;
import com.zzm.java_test.statictest.StaticClass;
import com.zzm.java_test.statictest.StaticTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class JavaTest {

    public static void main(String[] args) {
//        dynamicProxyTest();
//        synchronousQueueTest();H
//        staticTest();
        annotationTest();
    }

    private static void staticTest() {
//        Class a=StaticTest.class;//只会类加载不会初始化
//        a.getDeclaredFields();
//        System.out.print(a.toString());
//        System.out.print(StaticTest.i);//会类加载和初始化
//        StaticTest staticTest=new StaticTest();
//        StaticTest.doThings();
    }

    private static void annotationTest() {
        try {
            IplayImp iplayImp = new IplayImp();

            IplayImp iplayImp1 = new IplayImp();
            ((Play) iplayImp1).print();

            Play iplayImp2 = new IplayImp();
//            ((PlayImpl) iplayImp2).print();

            Method print = iplayImp.getClass().getDeclaredMethod("print");
            for (Annotation annotation : print.getAnnotations()) {
                System.out.print(annotation.toString());
            }
            GET annotation = print.getAnnotation(GET.class);
            System.out.print(annotation);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        List<String> list = new ArrayList<>();
        String a;
        for (int i = 0; i < 4; i++) {
            a = i + "ii";
            list.add(a);
        }
        a = "ddddd";
        for (int i = 0; i < list.size(); i++) {

            System.out.print(list.get(i));
        }

        Map<String, String> map = new HashMap<>();
        map.put("1", "2");
        for (Map.Entry<String, String> entry : map.entrySet()
        ) {
            System.out.print(entry.getValue() + "  " + entry.getKey());
        }
    }

    private static void dynamicProxyTest() {
        System.out.print("hello java\n");
        System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");
        IProxy proxyInstance = (IProxy) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{IProxy.class}
                , new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        for (Annotation a : method.getAnnotations()) {
                            System.out.print(a.toString());
                        }
                        return null;
                    }
                });
        proxyInstance.go("hello");
//        HashMap hashMap=new HashMap();
//        hashMap.put(0,0);
//        System.out.print(hashMap.);
        System.out.print(StaticClass.class.getName());
        System.out.print(StaticClass.class.getDeclaredMethods()[0].getName());
        System.out.print(StaticClass.class.getDeclaredMethods()[0].getParameterTypes()[0].getName() + "\n");
        StaticClass.get("");
    }

    private static void synchronousQueueTest() {
        //BlockingQueue<Runnable> queue = new SynchronousQueue<>();
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(10);
        if (queue.offer(new Runnable() {
            @Override
            public void run() {

            }
        }))
            System.out.print("queue offer ok");
        else
            System.out.print("queue offer no ok");
    }
}