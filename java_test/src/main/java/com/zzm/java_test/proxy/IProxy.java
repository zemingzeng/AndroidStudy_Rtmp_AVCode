package com.zzm.java_test.proxy;

import com.zzm.java_test.annotation.GET;

public interface IProxy {
    @GET(get=3434)
    String go(String a);
}
