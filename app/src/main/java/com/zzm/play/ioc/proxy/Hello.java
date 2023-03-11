package com.zzm.play.ioc.proxy;

import com.zzm.play.utils.l;

public class Hello implements IHello {
    @Override
    public void sayHello() {
        l.i("sayHello");
    }
}
