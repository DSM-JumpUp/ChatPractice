package com.jumpup.tails.studysocket;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.jumpup.tails.studysocket.dialog.LoginWaitDialog;
import com.jumpup.tails.studysocket.socket.SocketApplication;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_SUCCESS;

public class LoginActivity extends AppCompatActivity {

    private AppCompatEditText editText;
    private Socket mSocket;
    private String mUsername;
    private LoginWaitDialog dialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText = findViewById(R.id.appCompatEditText);
        AppCompatButton btn = findViewById(R.id.appCompatButton);
        mSocket = SocketApplication.getSocket();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryLogin();
            }
        });
        //mSocket.on("login", onLogin);
        mSocket.on("chat start", onChatStart);
    }

    private void tryLogin(){
        editText.setError(null);

        String username = editText.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            editText.setError("닉네임을 입력해주세요!!");
            editText.requestFocus();
            return;
        }

        mUsername = username;
        mSocket.emit("add user", username);

        FragmentManager fm = getSupportFragmentManager();
        dialogFragment = new LoginWaitDialog();
        dialogFragment.show(fm, "wait_fragment_dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mSocket.off("login", onLogin);
        mSocket.off("chat start", onChatStart);
    }

/*    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            final int UserNum;
            try {
                UserNum = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(),"로그인 성공!  유저번호 : " + UserNum + " 유저이름 : " + mUsername, Toast.LENGTH_SHORT).show();
                }
            });
            Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
            sendIntent.putExtra("userName", mUsername);
            setResult(LOGIN_SUCCESS, sendIntent);
            finish();
        }
    };*/

    private Emitter.Listener onChatStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            final String mRoom;
            try {
                mRoom = data.getString("room");
            } catch (JSONException e) {
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogFragment.dismiss();
                    Toast.makeText(getApplication(),"로그인 성공!  방 : " + mRoom + " 유저이름 : " + mUsername, Toast.LENGTH_SHORT).show();
                }
            });
            Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
            sendIntent.putExtra("userName", mUsername);
            sendIntent.putExtra("room", mRoom);
            setResult(LOGIN_SUCCESS, sendIntent);
            finish();
        }
    };
}
