package com.example.dsm2017.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import java.util.ArrayList;

public class ChattingActivity extends AppCompatActivity {
    private SocketApp socketApp; // application 단에 올린 socket class 선언
    private Socket mSocket; // Socket.io activity 선언
    //----------------------------------------------
    private String name;
    private TextView peerName;
    //----------------------------------------------
    private ArrayList<ChatData> mArrayList; // recycler view 에 올바른 데이터 셋을 담아줄 arrayList
    private ChatAdapter mAdapter; // recycler view 어뎁터 선언
    private RecyclerView mRecycler; // recycler view 뷰 선언
    //----------------------------------------------
    private EditText mChatText;
    private ImageButton mEditBtn;

    private LinearLayoutManager linearLayoutManager;

    private ImageButton exitBtn;
    private ImageButton siren;
    private ImageButton plusBtn;
    private ImageButton cancelBtn;

    private ConstraintLayout bottomSheet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        setPeerName(); // 상대 닉네임 설정
        //----------------------------------------------
        socketApp = (SocketApp) getApplication();
        mSocket = socketApp.getSocket(); // application에 올린 socket 불러오기

        mArrayList = new ArrayList<>(); // recycler view 데이터 셋 초기화

        mAdapter = new ChatAdapter(mArrayList); // 어뎁터 초기화

        mRecycler = findViewById(R.id.chatRecycler); // recycler view 뷰 지정
        mRecycler.setAdapter(mAdapter); // 뷰에 어뎁터 설정
        mRecycler.setLayoutManager(linearLayoutManager);
        mRecycler.setHasFixedSize(true);
        //----------------------------------------------
        mChatText = findViewById(R.id.chatInput); // 채팅 인풋부분 지정
        mEditBtn = findViewById(R.id.editBtn); // 전송 버튼 지정
        mEditBtn.setOnClickListener(sendMessage); // 전송 버튼 이벤트 설정
        //----------------------------------------------
        mSocket.on("message", recieveMessage);

        exitBtn = findViewById(R.id.exitBtn);
        siren = findViewById(R.id.siren);
        plusBtn = findViewById(R.id.plusBtn);
        bottomSheet = findViewById(R.id.bottom_sheet);
        cancelBtn = findViewById(R.id.cancelBtn);


        exitBtn.setOnClickListener(exitRoom);
        siren.setOnClickListener(report);
        plusBtn.setOnClickListener(takeUpSheet);
        cancelBtn.setOnClickListener(takeDownSheet);
    }


    private void setPeerName() {
        // 이전 화면에서 intent 이벤트와 넘겨준 상대방 랜덤 닉네임 값 가져와 설정해주는 함수
        Intent getName = getIntent();
        name = getName.getStringExtra("peerName");

        peerName = findViewById(R.id.peerName);
        peerName.setText(name);
    }


    private View.OnClickListener sendMessage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String inputText = mChatText.getText().toString().trim(); // 입력된 값을 가져와 String으로 처리하며 좌우 공백을 제외시킨다.
            if (TextUtils.isEmpty(inputText)) return; // 아무것도 입력하지 않았을 때를 필터링 해낸다.

            mChatText.setText("");
            mSocket.emit("message", inputText); // 소켓 전송
            mArrayList.add(new ChatData.Builder().Build(0, inputText));
            // 데이터 셋의 형식대로 값을 전달한다. Build 함수는 데이터 셋을 설정해주는 함수이다.
            mAdapter.notifyDataSetChanged(); // 값이 달라졌음을 알린다.

            scrollBottom();
        }
    };

    private void scrollBottom(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecycler.scrollToPosition(mAdapter.getItemCount() - 1);
            }
        });
    }

    private Emitter.Listener recieveMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String recieveData = (String) args[0];
                    // 받을 데이터가 String 값일 것이고 그 값을 전달 받아 recycler view에 전달 할 변수를 선언 및 대입
                    mArrayList.add(new ChatData.Builder().Build(1, recieveData));
                    mAdapter.notifyDataSetChanged();
                }
            });

            scrollBottom();
        }
    };

    private View.OnClickListener exitRoom = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mSocket.emit("leave room");
            finish();
        }
    };

    private View.OnClickListener report = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mSocket.emit("siren");
            Toast.makeText(getApplicationContext(), "상대방을 신고하셨습니다", Toast.LENGTH_SHORT).show();
        }
    };

    private View.OnClickListener takeUpSheet = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bottomSheet.setVisibility(View.VISIBLE);
            plusBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.VISIBLE);
        }
    };
    private View.OnClickListener takeDownSheet = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bottomSheet.setVisibility(View.GONE);
            plusBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.GONE);
        }
    };
}
