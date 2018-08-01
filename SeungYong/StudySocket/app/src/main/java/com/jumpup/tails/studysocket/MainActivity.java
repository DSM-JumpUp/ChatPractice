package com.jumpup.tails.studysocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private boolean misPlus = false;

    private ArrayList<Message> mMessages = new ArrayList<>();
    private ChatLog mChatLog;
    private RecyclerView mRecyclerView;

    private AppCompatEditText mTypeText;

    private TextView mUserNameTextView;

    private View chooseMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserNameTextView = findViewById(R.id.user_name);

        mRecyclerView = findViewById(R.id.recycler_chat);

        mTypeText = findViewById(R.id.type_chat_edit_text);

        Button mSendBtn = findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(sendChat);

        Button mPlusBtn = findViewById(R.id.send_plus);
        mPlusBtn.setOnClickListener(sendPlus);

        chooseMode = findViewById(R.id.choose_mode);

        mSocket = SocketApplication.getSocket();
        mSocket.connect();
        mSocket.on("message", NewMessage);
        mSocket.on("chat end", ChatEnd);

        startConnect();
    }

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
            String chatData = mTypeText.getText().toString().trim();
            if (!TextUtils.isEmpty(chatData)) {
                mSocket.emit("message", chatData);
                mTypeText.getText().clear();
            }
        }
    };

    private View.OnClickListener sendPlus = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!misPlus){
                misPlus = true;
                chooseMode.setVisibility(View.VISIBLE);
            }else {
                misPlus = false;
                chooseMode.setVisibility(View.GONE);
            }
        }
    };

    void addMessageLog(){
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() { mChatLog.notifyItemInserted(mMessages.size() - 1); }
        });
    }

    void scrollBottom(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() { mRecyclerView.scrollToPosition(mChatLog.getItemCount() - 1); }
        });
    }

    private Emitter.Listener NewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            final String chatData;
            final String id;

            try {
                chatData = data.getString("message");
                id = data.getString("id");
            } catch (JSONException e) { return; }

            if(mSocket.id().equals(id)) mMessages.add(new Message.Builder().Build(Message.TYPE_MESSAGE, chatData));
            else mMessages.add(new Message.Builder().Build(Message.TYPE_LOG, chatData));

            addMessageLog();
            scrollBottom();
        }
    };

    private Emitter.Listener ChatEnd = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() { Toast.makeText(getApplicationContext(), "상대방을 채팅을 종료했습니다. 첫 시작화면으로 돌아갑니다.", Toast.LENGTH_SHORT).show(); }
            });

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    };

    private long backPressedTime = 0;
    @Override
    public void onBackPressed(){
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        long FINISH_INTERVAL_TIME = 2000;
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
            mSocket.off("message", NewMessage);
            mSocket.off("chat end", ChatEnd);
            mSocket.disconnect();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 더 누르면 채팅이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
