package com.zzm.play.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zzm.play.IClientCallback;
import com.zzm.play.IRequest;
import com.zzm.play.R;
import com.zzm.play.utils.l;

public class MainActivity extends AppCompatActivity {
    static {
//        System.loadLibrary("play");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(this, "com.zzm.play.service.RemoteService");
        intent.setComponent(componentName);
        bindService(intent, new MyConnection(), BIND_AUTO_CREATE);
//        play();

    }

//    public static native void play();


    static class MyConnection implements ServiceConnection {
        private IClientCallback clientCallback;
        private IRequest iRequest;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            l.i("client ibinder hashCode---" + service.hashCode());
            try {
                iRequest = IRequest.Stub.asInterface(service);
                iRequest.registerCallback(clientCallback = new IClientCallback.Stub() {
                    @Override
                    public void callback(String msg) throws RemoteException {
                        l.i("onServiceConnected IClientCallback 回到接收到的消息：" + msg);
                    }

                    @Override
                    public IBinder asBinder() {
                        l.i("IClientCallback.Stub asBinder");
                        return super.asBinder();
                    }
                });

                iRequest.set(2);

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                if (null != clientCallback) {
                    iRequest.unRegisterCallback(clientCallback);
                    clientCallback = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onBindingDied(ComponentName name) {
            try {
                if (null != clientCallback) {
                    iRequest.unRegisterCallback(clientCallback);
                    clientCallback = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onNullBinding(ComponentName name) {
            try {
                if (null != clientCallback) {
                    iRequest.unRegisterCallback(clientCallback);
                    clientCallback = null;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

}