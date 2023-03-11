package com.zzm.play.ioc;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.ioc.proxy.Hello;
import com.zzm.play.ioc.proxy.IHello;
import com.zzm.play.utils.l;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class IOCActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ioc_activity_layout);

        other();

    }

    private void other() {

        Hello hello = new Hello();

        IHello proxy = (IHello) Proxy.newProxyInstance(getClassLoader(), new Class[]{IHello.class}, new ProxyHandler(hello));

        proxy.sayHello();
    }

    static class ProxyHandler implements InvocationHandler {

        private final Object needProxy;

        public ProxyHandler(Object needProxy) {
            this.needProxy = needProxy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            l.i("before");
            Object o = method.invoke(needProxy, args);
            l.i("after");

            return o;
        }
    }
}
