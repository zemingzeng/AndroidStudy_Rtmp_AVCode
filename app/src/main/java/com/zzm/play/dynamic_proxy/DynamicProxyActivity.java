package com.zzm.play.dynamic_proxy;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zzm.play.R;
import com.zzm.play.utils.l;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import retrofit2.Retrofit;

public class DynamicProxyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test_layout);

        try {
            doSomething();
        } catch (NoSuchMethodException e) {
            l.i(e.toString());
        }
    }

    private void doSomething() throws NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod("proxyMethod", String.class);
        method.setAccessible(true);
        IProxy proxyInstance = (IProxy) Proxy.newProxyInstance(this.getClassLoader(),
                new Class[]{IProxy.class},
                new InvocationHandlerImp(this, method));
        Request request = new Request();
        request.setiProxy(proxyInstance);
        request.go("hello");

        new Retrofit.Builder().build();
    }

    private String proxyMethod(String a) {
        l.i("proxyMethod : " + a);
        return a;
    }

    static final class InvocationHandlerImp implements InvocationHandler {
        private final Object requestObj;
        private final Method method;

        InvocationHandlerImp(Object requestObj, Method method) {
            this.requestObj = requestObj;
            this.method = method;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object invoke = this.method.invoke(requestObj, args);
            l.i("proxy:\n" + proxy.getClass().toString() + "\n" +
                    "this.method:\n" + this.method + "\n" +
                    " method:\n" + method + "\n" +
                    " args:\n" + Arrays.toString(args) + "\n" +
                    " return:\n" + invoke);
            //当invoke返回void类型时能够被转成对应类型的null值
            return invoke;
        }
    }
}
