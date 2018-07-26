package dasilver.jeong.chatpracticeandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private SimpleDateFormat timeDateFormat, dateDateFormat;
    private String timeText, dateText;

    private Button sendMessageButton;
    private EditText messageEditText;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        String room = intent.getExtras().getString("room");
//        Toast.makeText(getApplicationContext(),room,Toast.LENGTH_SHORT).show();

        sendMessageButton = (Button) findViewById(R.id.btn_chat_send_message);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        data = new JSONObject();


        now = System.currentTimeMillis();
        nowDate = new Date(now);
        timeDateFormat = new SimpleDateFormat("hh:mm");
        dateDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        timeText = timeDateFormat.format(nowDate);
        dateText = dateDateFormat.format(nowDate);


        chatRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
        chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatRecyclerItems = new ArrayList();

        chatRecycler.setLayoutManager(chatLayoutManager);
        chatRecycler.setItemAnimator(new DefaultItemAnimator());
        chatRecyclerAdapter = new ChatRecyclerViewAdapter(chatRecyclerItems);

        chatRecycler.setAdapter(chatRecyclerAdapter);

        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();

        socket.on("message", receiveMassage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("message", messageEditText.getText().toString());
                    socket.emit("message", data);
                    chatRecyclerItems.add(new ChatRecyclerItem(0, dateText));
                    chatRecyclerItems.add(new ChatRecyclerItem(1, messageEditText.getText().toString(), timeText));
                    chatRecyclerAdapter.notifyDataSetChanged();
                    chatRecycler.smoothScrollToPosition(chatRecyclerItems.size() - 1);
                    messageEditText.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                    String yourMessage;
//                    ArrayList<ChatRecyclerItem> chatRecyclerItems = new ArrayList();

                    try {
                        yourMessage = data.getString("message");
                        Log.d("Message", yourMessage);
                        chatRecyclerItems.add(new ChatRecyclerItem(0, dateText));
                        chatRecyclerItems.add(new ChatRecyclerItem(2, yourMessage, timeText));
                        chatRecyclerAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("Receive Message");
    }
}
