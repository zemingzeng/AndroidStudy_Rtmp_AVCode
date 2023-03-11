package com.zzm.play.dynamic_proxy;

import com.zzm.play.utils.l;

public class Request {
    private IProxy iProxy;

    public void setiProxy(IProxy iProxy) {
        this.iProxy = iProxy;
    }

    public void go(String a) {
        if (null != iProxy)
            l.i("iProxy.go:" + iProxy.go(a));
    }
}
