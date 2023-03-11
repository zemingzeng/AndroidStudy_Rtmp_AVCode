package com.zzm.java_test.statictest;

public class StaticClass {
    static class aa {
    }

    public static void get(String a) {
        Object[] objects = new Object[2];
        objects[0] = "ddd";
        objects[1] = 3;
        System.out.print(objects[0] + "  " + objects[1]);
    }

}
