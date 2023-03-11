package com.zzm.play.ioc.proxy;

public interface IHello {
    /**
     * 1.生成class
     * 2.加载class(native)
     * 3.生成对象（reflect）
     * 动态代理的用处：sdk,用于扩展性,屏蔽底层实现
     */
    void sayHello();
}
