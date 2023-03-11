package com.zzm.play.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import androidx.annotation.Nullable;

import com.zzm.play.IClientCallback;
import com.zzm.play.IRequest;
import com.zzm.play.utils.l;

public class RemoteService extends Service {
    private final RemoteCallbackList<IClientCallback> list = new RemoteCallbackList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
         IBinder ibinder=new IRequest.Stub() {
            @Override
            public int set(int a) throws RemoteException {

                l.i("RemoteService set ");

                int broadcasts = list.beginBroadcast();
                for (int i = 0; i < broadcasts; i++) {
                    list.getBroadcastItem(i).callback("我是callback" + i + " 向客户端发送回调");
                }

                return 123;
            }

            @Override
            public void registerCallback(IClientCallback callback) throws RemoteException {
                l.i("RemoteService  registerCallback");
                if (null != callback)
                    list.register(callback);
            }

            @Override
            public void unRegisterCallback(IClientCallback callback) throws RemoteException {
                l.i("RemoteService  unRegisterCallback");
                if (null != callback)
                    list.unregister(callback);
            }
        };
        l.i("server ibinder hashCode---" + ibinder.hashCode());
        return ibinder;
    }
}
