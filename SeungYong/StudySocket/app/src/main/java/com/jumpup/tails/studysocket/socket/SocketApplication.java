package com.jumpup.tails.studysocket.socket;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketApplication extends Application{
    public static final int LOGIN_TRY = 0;
    public static final int LOGIN_SUCCESS = 1;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://10.0.2.2:3000/");
           // mSocket = IO.socket("http://10.156.145.113:3000/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
