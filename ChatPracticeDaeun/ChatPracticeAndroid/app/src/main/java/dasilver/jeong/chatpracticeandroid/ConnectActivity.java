package dasilver.jeong.chatpracticeandroid;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectActivity extends AppCompatActivity {

    private Button startButton;
    private Socket socket;
    private String room;
    private ConnectDialog connectDialog;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startButton = (Button) findViewById(R.id.btn_connect_start);
        mHandler = new Handler();
        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();
        socket.connect();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("login");
                socket.on("chat start", chatStart);
                connectDialog();
            }
        });
    }

    private Emitter.Listener chatStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("Debug","chatStart");
            runOnUiThread(new Runnable() {
                JSONObject data = (JSONObject) args[0];
                @Override
                public void run() {
                    try {
                        room = data.getString("room");
                        Intent intent = new Intent(ConnectActivity.this, ChatActivity.class);
                        intent.putExtra("room", room);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }
    };

    private void connectDialog() {
        connectDialog = new ConnectDialog(ConnectActivity.this, cancelClickListener);
        connectDialog.setCancelable(true);
        connectDialog.getWindow().setGravity(Gravity.CENTER);
        connectDialog.show();
    }

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectDialog.dismiss();
        }
    };

}
