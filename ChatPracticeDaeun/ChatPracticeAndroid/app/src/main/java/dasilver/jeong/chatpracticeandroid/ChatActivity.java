package dasilver.jeong.chatpracticeandroid;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

    private RecyclerView chatRecycler;
    private LinearLayoutManager chatLayoutManager;
    private ChatRecyclerViewAdapter chatRecyclerAdapter;
    private ArrayList<ChatRecyclerItem> chatRecyclerItems = null;
    private long now;
    private Date nowDate;
    private SimpleDateFormat timeDateFormat;
    private String timeText, nickName;
    private ImageButton sendMessageButton, plusButton, cancelButton, leaveButton;
    private EditText messageEditText;
    private TextView nickNameText;
    private JSONObject data;
    private ConstraintLayout bottomBtnLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        String room = intent.getExtras().getString("room");
//        Toast.makeText(getApplicationContext(),room,Toast.LENGTH_SHORT).show();

        sendMessageButton = (ImageButton) findViewById(R.id.btn_chat_send_message);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        nickNameText = (TextView) findViewById(R.id.text_chat_nickname);
        plusButton = (ImageButton)findViewById(R.id.btn_chat_plus);
        cancelButton = (ImageButton)findViewById(R.id.btn_chat_cancel);
        bottomBtnLayout = (ConstraintLayout)findViewById(R.id.layout_bottom_btn);
        leaveButton = (ImageButton)findViewById(R.id.btn_chat_leave);
        data = new JSONObject();
        chatRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
        chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecyclerItems = new ArrayList();

        chatRecycler.setLayoutManager(chatLayoutManager);
        chatRecycler.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerAdapter = new ChatRecyclerViewAdapter(chatRecyclerItems);

        chatRecycler.setAdapter(chatRecyclerAdapter);
        nickNameText.setText(setNickName());

        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();

        socket.on("message", receiveMassage);
        socket.on("chat end", chatEnd);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("Debug","message");
                    data.put("message", messageEditText.getText().toString());
                    socket.emit("message", data);
                    chatRecyclerItems.add(new ChatRecyclerItem(0, messageEditText.getText().toString(), getTimeText()));
                    chatRecyclerAdapter.notifyDataSetChanged();
                    chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                    messageEditText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomBtnLayout.setVisibility(View.VISIBLE);
                plusButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.VISIBLE);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomBtnLayout.setVisibility(View.GONE);
                plusButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.GONE);
            }
        });

        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("leave room");
                Log.d("Debug","leave Button clicked");
                Intent intent = new Intent(ChatActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });

    }


    private Emitter.Listener receiveMassage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String yourMessage, yourTime;
//                    ArrayList<ChatRecyclerItem> chatRecyclerItems = new ArrayList();

                    try {
                        yourMessage = data.getString("message");
                        chatRecyclerItems.add(new ChatRecyclerItem(1, yourMessage, getTimeText()));
                        chatRecyclerAdapter.notifyDataSetChanged();
                        chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener chatEnd = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Debug", "chatEnd");
                    chatRecyclerItems.add(new ChatRecyclerItem(2, nickName));
                    chatRecyclerAdapter.notifyDataSetChanged();
                    chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                }
            });
        }
    };

    private String getTimeText() {
        now = System.currentTimeMillis();
        nowDate = new Date(now);
        timeDateFormat = new SimpleDateFormat("hh:mm");
        timeText = timeDateFormat.format(nowDate);

        return timeText;
    }

    private String setNickName() {
        String[] nickNameList = {"익명의 너구리", "익명의 황조새", "익명의 손승용", "익명의 아스파라거스", "익명의 바퀴벌레", "익명의 파파야", "익명의 땅다람쥐", "익명의 친칠라"};
        Random r = new Random();
        nickName = nickNameList[r.nextInt(nickNameList.length)];
        return nickName;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("Receive Message");
    }
}
