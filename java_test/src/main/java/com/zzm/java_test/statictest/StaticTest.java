package com.zzm.java_test.statictest;

public class StaticTest {

public int a=1;
    {
        System.out.print("no StaticTest static codes");
        i=2;
    }
    static {
        System.out.print("StaticTest static codes");
        i=2;
    }
    public static  int i=1;
    static {
        i=3;
    }
    public static void doThings(){
        System.out.print("StaticTest doThings");
        System.out.print("StaticTest i ="+i);
    }
}
