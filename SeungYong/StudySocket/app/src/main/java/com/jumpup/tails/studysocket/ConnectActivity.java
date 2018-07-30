package com.jumpup.tails.studysocket;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.jumpup.tails.studysocket.dialog.LoginWaitDialog;
import com.jumpup.tails.studysocket.socket.SocketApplication;

import java.util.Random;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_SUCCESS;

public class ConnectActivity extends AppCompatActivity {

    private Socket mSocket;
    private LoginWaitDialog dialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Button btn = findViewById(R.id.btn_connect_start);
        mSocket = SocketApplication.getSocket();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { tryMatching(); }
        });
        mSocket.on("chat start", onChatStart);
    }


    private String getUsername(){
        Resources res = getResources();
        String[] names = res.getStringArray(R.array.names_array);
        Random rnd = new Random();
        rnd.nextInt();
        return names[rnd.nextInt(names.length-1)];
    }


    private void tryMatching(){
        mSocket.emit("add user");

        FragmentManager fm = getSupportFragmentManager();
        dialogFragment = new LoginWaitDialog();
        dialogFragment.setCancelable(false);
        dialogFragment.show(fm, "wait_fragment_dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("chat start", onChatStart);
    }

    private Emitter.Listener onChatStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() { dialogFragment.dismiss(); }
            });
            Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
            sendIntent.putExtra("userName", getUsername());
            setResult(LOGIN_SUCCESS, sendIntent);
            finish();
        }
    };
}
