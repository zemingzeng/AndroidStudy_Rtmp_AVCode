package com.zzm.play.projection_screen.projection_screen_server;

import com.zzm.play.utils.l;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;

//局域网同信 服务端 开启就调用start就好
public class Server extends WebSocketServer {
    private WebSocket webSocket;

    public Server(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        //客户端链接成功
        l.i("WebSocketServer->onOpen->RemoteSocketAddress: " + conn.getRemoteSocketAddress());
        this.webSocket = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        l.i("WebSocketServer->onClose->reason : " + reason + "  remote : " + remote);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        l.i("WebSocketServer->onMessage : " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        l.i("WebSocketServer->onError : " + ex.toString());

    }

    @Override
    public void onStart() {
        //服务端启动成功
        l.i("WebSocketServer->onStart");
    }

    //推送数据
    public void sendData(byte[] bytes) {
        if (null != webSocket && webSocket.isOpen())
            webSocket.send(bytes);
    }

    public boolean isOpen() {
        if (null != webSocket)
            return webSocket.isOpen();
        return false;
    }

    public void startMe() {
        start();
    }

    //关闭资源
    public void closeMe() throws IOException, InterruptedException {
        if (null != webSocket)
            webSocket.close();
        stop();
    }
}
