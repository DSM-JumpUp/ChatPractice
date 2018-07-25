package com.jumpup.tails.studysocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jumpup.tails.studysocket.socket.SocketApplication;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_SUCCESS;
import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_TRY;

public class MainActivity extends AppCompatActivity {
    private String mUserName;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketApplication socketApplication = (SocketApplication) getApplication();
        mSocket = socketApplication.getSocket();
        mSocket.connect();
        mSocket.on("user joined", userJoined);
        mSocket.on("typing", Typing);
        mSocket.on("stop typing", StopTyping);
        mSocket.on("user left", UserLeft);
        mSocket.on("new message", NewMessage);
        startLogin();
    }

    private Emitter.Listener userJoined = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener Typing = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener StopTyping = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener UserLeft = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    private Emitter.Listener NewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    void startLogin(){
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(i,LOGIN_TRY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LOGIN_TRY){
            if (resultCode == LOGIN_SUCCESS){
                mUserName = data.getStringExtra("userName");
                Log.d("Intent", mUserName);
            }else{
                Log.d("Intent", "NOOOO");
            }
        }
    }


}
