package dasilver.jeong.chatpracticeandroid;

import android.content.Intent;
import android.media.Image;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class ChatActivity extends AppCompatActivity {

    private Socket socket;

    private RecyclerView chatRecycler; //채팅 목록을 보여주는 RecyclerView
    private LinearLayoutManager chatLayoutManager;
    private ChatRecyclerViewAdapter chatRecyclerAdapter;
    private ArrayList<ChatRecyclerItem> chatRecyclerItems = null;
    private long now;
    private Date nowDate;
    private SimpleDateFormat timeDateFormat;
    private String timeText, nickName;
    private ImageButton sendMessageButton, plusButton, cancelButton, leaveButton, reportButton;
    private EditText messageEditText;
    private TextView nickNameText;
    private JSONObject data;
    private ConstraintLayout bottomBtnLayout;
    private ChatReportDialog chatReportDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();

        sendMessageButton = (ImageButton) findViewById(R.id.btn_chat_send_message); //메시지 전송 버튼
        messageEditText = (EditText) findViewById(R.id.edit_chat_message); //메시지를 적는 EditText
        nickNameText = (TextView) findViewById(R.id.text_chat_nickname); //닉네임을 보여주는 Toolbar에 있는 TextView
        plusButton = (ImageButton)findViewById(R.id.btn_chat_plus); //추가 버튼들을 보여주는 버튼
        cancelButton = (ImageButton)findViewById(R.id.btn_chat_cancel); //추가 버튼들을 숨기는 버튼
        bottomBtnLayout = (ConstraintLayout)findViewById(R.id.layout_bottom_btn); //추가 버튼들이 있는 숨겨진 레이아웃
        leaveButton = (ImageButton)findViewById(R.id.btn_chat_leave); //채팅 방 나가기 버튼
        reportButton = (ImageButton)findViewById(R.id.btn_chat_report);
        data = new JSONObject();
        chatRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
        chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecyclerItems = new ArrayList();

        chatRecycler.setLayoutManager(chatLayoutManager);
        chatRecycler.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerAdapter = new ChatRecyclerViewAdapter(chatRecyclerItems);

        chatRecycler.setAdapter(chatRecyclerAdapter);
        nickNameText.setText(setNickName()); //Toolbar에 있는 상대방의 닉네임 설정

        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();

        //Server로부터 message 받기를 대기
        socket.on("message", receiveMassage);
        //Server로부터 chat end 받기를 대기
        socket.on("chat end", chatEnd);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("Debug","message");
                    //data에 메시지를 담아 Server로 전송
                    data.put("message", messageEditText.getText().toString());
                    socket.emit("message", data);
                    //채팅 목록에 내가 보낸 메시지 띄워주기
                    chatRecyclerItems.add(new ChatRecyclerItem(0, messageEditText.getText().toString(), getTimeText()));
                    //채팅 목록 갱신
                    chatRecyclerAdapter.notifyDataSetChanged();
                    //채팅 목록이 가장 최근 메시지를 가르키도록 설정
                    chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                    //메시지를 작성하는 EditText 초기화
                    messageEditText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //plusButton을 클릭하였을 경우 숨겨진 버튼들이 보여짐
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomBtnLayout.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });

        //cancelButton을 클릭하였을 경우 보여졌던 버튼들이 다시 숨겨짐
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomBtnLayout.setVisibility(View.GONE);
                plusButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
            }
        });

        //채팅방 나가기 버튼을 클릭하였을 경우 Server로 leave room전송 후 ConnectActivity로 이동
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("leave room");
                Log.d("Debug","leave Button clicked");
                Intent intent = new Intent(ChatActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatReportDialog = new ChatReportDialog(ChatActivity.this, reportClickListener, cancelClickListener);
                chatReportDialog.setCancelable(true);
                chatReportDialog.getWindow().setGravity(Gravity.CENTER);
                chatReportDialog.show();
            }
        });
    }

    private View.OnClickListener reportClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //todo EditText에 있는 데이터 Server로 전송
            socket.emit("report");
            Toast.makeText(getApplicationContext(),"신고가 완료되었습니다.",Toast.LENGTH_SHORT).show();
            chatReportDialog.dismiss();
        }
    };

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            chatReportDialog.dismiss();
        }
    };

    //Server로 부터 recieve message를 받았을 경우 실행할 메서드
    private Emitter.Listener receiveMassage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0]; //서버로부터 받는 JSON 데이터
                    String yourMessage;

                    try {
                        //yourMessage에 서버로부터 받은 데이터에서 message를 얻어와 채팅 목록에 띄워주기
                        yourMessage = data.getString("message");
                        chatRecyclerItems.add(new ChatRecyclerItem(1, yourMessage, getTimeText()));
                        //채팅 목록 갱신
                        chatRecyclerAdapter.notifyDataSetChanged();
                        //채팅 목록이 가장 최근 메시지를 가르키도록 설정
                        chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    //Server로부터 chat end를 받았을 경우 실행할 메서드
    private Emitter.Listener chatEnd = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Debug", "chatEnd");
                    //채팅 목록에 상대방이 나갔다는 문구 띄워주기
                    chatRecyclerItems.add(new ChatRecyclerItem(2, nickName));
                    //채팅 목록 갱신
                    chatRecyclerAdapter.notifyDataSetChanged();
                    //채팅 목록이 가장 최근 메시지를 가르키도록 설정
                    chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                }
            });
        }
    };

    //현재 시간 구하는 메서드
    private String getTimeText() {
        now = System.currentTimeMillis();
        nowDate = new Date(now);
        timeDateFormat = new SimpleDateFormat("hh:mm");
        timeText = timeDateFormat.format(nowDate);

        return timeText;
    }

    //닉네임을 설정해주는 메서드
    private String setNickName() {
        String[] nickNameList = {"익명의 너구리", "익명의 황조새", "익명의 손승용", "익명의 아스파라거스", "익명의 바퀴벌레", "익명의 파파야", "익명의 땅다람쥐", "익명의 친칠라"};
        Random r = new Random();
        nickName = nickNameList[r.nextInt(nickNameList.length)];
        return nickName;
    }

    //Destroy됐을 경우 socket통신 끝내기
    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("Receive Message");
    }
}
