package com.zzm.play.projection_screen.projection_screen_client;

import com.zzm.play.utils.l;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class Client extends WebSocketClient {
    public Client(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        l.i("WebSocketClient->onOpen->getHttpStatusMessage: " + handshake.getHttpStatusMessage());
    }

    @Override
    public void onMessage(String message) {
        l.i("WebSocketClient->onMessage String : " + message);
    }

    @Override
    public void onMessage(ByteBuffer byteBuffer) {
        //真实有效的数据长度
        int length = byteBuffer.remaining();
        l.i("WebSocketClient->onMessage Bytes: " + length);
        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);
        if (null != dataCallBack)
            dataCallBack.getData(bytes);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        l.i("WebSocketClient->onClose reason: " + reason + "  remote:" + remote);
    }

    @Override
    public void onError(Exception ex) {
        l.i("WebSocketClient->onError : " + ex.toString());
    }

    public void startMe() {
        connect();
    }

    public void closeMe() {
        close();
    }

    private DataCallBack dataCallBack;

    public void setDataCallBack(DataCallBack dataCallBack) {
        this.dataCallBack = dataCallBack;
    }

    interface DataCallBack {
        void getData(byte[] bytes);
    }



}
