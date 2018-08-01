package com.example.dsm2017.android;

import android.app.Application;
import android.util.Log;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class SocketApp extends Application {
    private Socket mSocket;
    @Override
    public void onCreate() {
        super.onCreate();


        try {
            mSocket = IO.socket("https://amuguna.herokuapp.com/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        };
    }
    public Socket getSocket() {
        return mSocket;
    }
}
