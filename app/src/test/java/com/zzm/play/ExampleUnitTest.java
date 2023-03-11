package com.zzm.play;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import android.os.Handler;

import com.zzm.play.proxy.IProxy;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        System.out.print("测试\n");
        A a = null;
        A a1 = a;
        A a2 = new A();
        a = a2;
        System.out.print(a1);
    }

    class A {

    }

    @Test
    public void bytesRotateTest() {

        int width = 4;
        int height = 6;

        int[] bytes = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            bytes[i] = i + 1;
        }


        YUV420BytesClockwise90Rotate(bytes, width, height, 0);
    }

    public int[] YUV420BytesClockwise90Rotate(int[] bytes, int width, int height, int yuv_type) {

        int totalSize = width * height;
        int ySize = width * (height - 2);
        int[] temp = new int[totalSize];

        //Y rotate
        int index = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height - 2; j++) {
                temp[index++] = bytes[i + width * (height - 2 - j - 1)];
            }
        }

        //u v rotate
        //sp类型 uv看成整体
        int uvHeight = (height - 2) / 2;
        int uvWidth = width / 2;
        for (int i = 0; i < uvWidth; i++) {
            for (int j = 0; j < uvHeight; j++) {

                int i1 = (i + uvWidth * (uvHeight - j - 1)) * 2;
                temp[index++] = bytes[i1 + ySize];
                temp[index++] = bytes[i1 + 1 + ySize];

            }
        }

        System.out.println(" 原始的数组：");
        println(bytes, width, height);
        System.out.println(" 顺时针旋转后的数组：");
        println(temp, height - 2, width + 2);


        return temp;
    }


    private void println(int[] bytes, int width, int height) {

        int index = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                System.out.print(bytes[index++] + "  ");

            }
            System.out.print("\n");
        }

    }

    @Test
    public void test1() {

        AA a = new AA();
        ((BB) a).aa();

    }

    class AA {
        void aa() {

        }
    }

    class BB extends AA {
        void bb() {

        }
    }

    @Test
    public void test2() {
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        ExecutorService cachedThreadPool1 = new ThreadPoolExecutor(2, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        for (int i = 0; i < 10; i++) {
            final int index = i;
            try {
//                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cachedThreadPool1.execute(new Runnable() {

                @Override
                public void run() {
                    System.out.println(index + "当前线程" + Thread.currentThread().getName());
                }
            });
        }
    }

    @Test
    public void test3() {


        List<Integer> list = new ArrayList<>();

        list.add(1);
        list.add(2);
        list.add(3);

        for (int i = 0; i < list.size(); i++) {
            System.out.println("--------" + list.get(i));
        }

    }

    @Test
    public void test4() throws InterruptedException {

        Thread thread;
        CC cc = new CC();
        for (int i = 0; i < 7; i++) {
            thread = new MyThread(cc);
            thread.start();
        }
        Thread.currentThread().join(20 * 1000);
    }

    class MyThread extends Thread {
        CC cc;

        public MyThread(CC cc) {
            this.cc = cc;
        }

        @Override
        public void run() {
            super.run();
            try {
                cc.aa();
                cc.bb();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class CC {

        public void aa() throws InterruptedException {
            // System.out.println(" aa start--" + Thread.currentThread().getName());

            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " aa sleep --");
                Thread.sleep(1000);
            }

            //System.out.println(" aa end--" + Thread.currentThread().getName());
        }

        public void bb() throws InterruptedException {
            //System.out.println(" bb start--" + Thread.currentThread().getName());

            synchronized (this) {
                System.out.println(Thread.currentThread().getName() + " bb sleep --");
                Thread.sleep(1000);
            }

            //System.out.println(" bb end--" + Thread.currentThread().getName());
        }
    }

    @Test
    public void test5() throws FileNotFoundException {
        DD dd = new DD();
        dd.ee.setA(100);
        DD dd1 = new DD();
        System.out.println("dd1->ee->a:" + dd1.ee.getA());
        System.out.println("dd->ee->a:" + dd.ee.getA());
        com.zzm.play.EE ee = new com.zzm.play.EE();
//        ee.a;
//        FileOutputStream outputStream = new FileOutputStream("");
//        outputStream.write();
//        outputStream.flush();
//        FileInputStream fileInputStream=new FileInputStream("");
//        fileInputStream.read()
    }

    private class DD {
        private volatile EE ee = new EE(99);
    }

    class EE {
        private int a = 99;

        public EE(int a) {
            this.a = a;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }
    }

    @Test
    public void test6() {
        FF ff = new FF();
        ff.hashCode();
    }

    static class FF {

    }

    @Test
    public void test7() {
        System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true");
        IProxy proxyInstance = (IProxy) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{IProxy.class}
                , new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });
        proxyInstance.go("hello");
    }

}

