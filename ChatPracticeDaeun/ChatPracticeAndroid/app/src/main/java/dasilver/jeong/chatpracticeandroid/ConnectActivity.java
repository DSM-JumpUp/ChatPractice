package dasilver.jeong.chatpracticeandroid;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectActivity extends AppCompatActivity {

    private Button startButton;
    private Socket socket;
    private String room;
    private ConnectDialog connectDialog;
    private ConnectFailDialog connectFailDialog;
    private ConnectAgainDialog connectAgainDialog;
    private Handler mHandler;
    private ProgressBar connectAgainProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startButton = (Button) findViewById(R.id.btn_connect_start);
        mHandler = new Handler();
        connectAgainProgressBar = (ProgressBar)findViewById(R.id.progress_connect_again);

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
            Log.d("Debug", "chatStart");
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

        Runnable mMyTask = new Runnable() {
            @Override
            public void run() {
                socket.emit("pop queue");
                connectDialog.dismiss();
                connectAgain100Dialog();
            }
        };
        mHandler.postDelayed(mMyTask, 5000);//test 추후에 10000으로 바꿀 예정

    }

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectDialog.dismiss();
        }
    };


    private void connectAgain100Dialog() {
        connectAgainDialog = new ConnectAgainDialog(ConnectActivity.this, "100", "200", againCancelClickListener);
        connectAgainDialog.setCancelable(true);
        connectAgainDialog.getWindow().setGravity(Gravity.CENTER);
        connectAgainDialog.show();

        Runnable mMyTask = new Runnable() {
            @Override
            public void run() {
                socket.emit("pop queue");
                connectAgainDialog.dismiss();
                connectAgain200Dialog();
            }
        };
        mHandler.postDelayed(mMyTask, 5000);//test 추후에 10000으로 바꿀 예정
    }

    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectAgainDialog.dismiss();
        }
    };
    private void connectAgain200Dialog() {
        connectAgainDialog = new ConnectAgainDialog(ConnectActivity.this, "200", "400", againCancelClickListener);
        connectAgainDialog.setCancelable(true);
        connectAgainDialog.getWindow().setGravity(Gravity.CENTER);
        connectAgainDialog.show();

        Runnable mMyTask = new Runnable() {
            @Override
            public void run() {
                socket.emit("pop queue");
                connectAgainDialog.dismiss();
                connectFailDialog();
            }
        };
        mHandler.postDelayed(mMyTask, 5000);//test 추후에 10000으로 바꿀 예정
    }

    private void connectFailDialog() {
        connectFailDialog = new ConnectFailDialog(ConnectActivity.this, failCancelClickListener);
        connectFailDialog.setCancelable(true);
        connectFailDialog.getWindow().setGravity(Gravity.CENTER);
        connectFailDialog.show();
    }
    private View.OnClickListener failCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectFailDialog.dismiss();
        }
    };

}
