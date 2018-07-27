package com.jumpup.tails.studysocket;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jumpup.tails.studysocket.adapter.ChatLog;
import com.jumpup.tails.studysocket.model.Message;
import com.jumpup.tails.studysocket.socket.SocketApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_SUCCESS;
import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_TRY;

public class MainActivity extends AppCompatActivity {
    private Socket mSocket;

    private boolean misSending = false;

    private ArrayList<Message> mMessages;
    private ChatLog mChatLog;
    private RecyclerView mRecyclerView;

    private AppCompatEditText mTypeText;

    private TextView mUserNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserNameTextView = findViewById(R.id.user_name);

        mMessages = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recycler_chat);

        mTypeText = findViewById(R.id.type_chat_edit_text);
        Button mSendBtn = findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(sendChat);

        mSocket = SocketApplication.getSocket();
        mSocket.connect();
        mSocket.on("message", NewMessage);
        //startLogin();
        startConnect();
    }

/*    void startLogin(){
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(i,LOGIN_TRY);
    }*/

    void startConnect(){
        Intent i = new Intent(MainActivity.this, ConnectActivity.class);
        startActivityForResult(i, LOGIN_TRY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == LOGIN_TRY){
            if (resultCode == LOGIN_SUCCESS){
                String mUserName = data.getStringExtra("userName");
                Log.d("Intent", mUserName);
                mUserNameTextView.setText(mUserName);

                mChatLog = new ChatLog(mMessages);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(mChatLog);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            }else{
                Log.d("Intent", "NOOOO");
            }
        }
    }

    private View.OnClickListener sendChat = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mTypeText.setError(null);

            String chatData = mTypeText.getText().toString().trim();
            if (TextUtils.isEmpty(chatData)) {
/*                mTypeText.setError("대화 내용을 입력해주세요!!");
                mTypeText.requestFocus();*/
                return;
            }else {
                mTypeText.getText().clear();
            }
            misSending  = true;
            mSocket.emit("message", chatData);
        }
    };


    private Emitter.Listener NewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            final String chatData;
            try {
                chatData = data.getString("message");
            } catch (JSONException e) {
                return;
            }
            Log.d("NEW MESSAGE", chatData);
            if(misSending){
                mMessages.add(new Message.Builder().Build(Message.TYPE_MESSAGE, chatData));
                misSending = false;
            }else{
                mMessages.add(new Message.Builder().Build(Message.TYPE_LOG, chatData));
                Log.d("LOGGG", "YEASDF");
            }
            mChatLog.notifyItemInserted(mMessages.size() - 1);
            scrollBottom();
        }
    };

    void scrollBottom(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() { mRecyclerView.scrollToPosition(mChatLog.getItemCount() - 1); }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mSocket.off("login", onLogin);
        mSocket.off("message", NewMessage);
        mSocket.disconnect();
    }
}
