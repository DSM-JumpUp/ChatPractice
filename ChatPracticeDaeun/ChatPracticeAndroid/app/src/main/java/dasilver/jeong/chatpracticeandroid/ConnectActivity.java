package dasilver.jeong.chatpracticeandroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectActivity extends AppCompatActivity {

    private Button startButton; //채팅 시작 버튼
    private Socket socket; //소켓
    private ConnectDialog connectDialog; //100m연결 다이얼로그
    private ConnectFailDialog connectFailDialog; //연결실패 다이얼로그
    private ConnectAgainDialog connectAgain200Dialog, connectAgain400Dialog; //200m연결 다이얼로그, 400m연결 다이얼로그
    private Handler mHandler;
    private LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkEnabled; //GPS와 Network를 허용했는지 체크하기 위한 변수
    private JSONObject data;
    private double lat, lng, distance; //위도, 경도, 거리를 나타내는 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        startButton = (Button) findViewById(R.id.btn_connect_start);
        mHandler = new Handler();
        data = new JSONObject();

        SocketApplication app = (SocketApplication) getApplication();
        socket = app.getSocket();
        socket.connect();

        //Server로부터 chat start를 받기를 대기
        socket.on("chat start", chatStart);
        //Server로부터 connect fail을 받기를 대기
        socket.on("connect fail", connectFail);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //GPS가 안켜져있을 경우 GPS를 켜라는 토스트메시지와 함께 GPS를 켤 수 있는 액티비티 띄워줌
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getApplicationContext(),"GPS를 켜세요",Toast.LENGTH_SHORT).show();
            Runnable mMyTask = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    //GPS 설정화면으로 이동
                    startActivity(intent);
                }
            };
            mHandler.postDelayed(mMyTask, 1500);
        }

        Log.d("Debug", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Debug", "isNetworkEnabled=" + isNetworkEnabled);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();

                Log.d("Location", "latitude: " + lat + ", longitude: " + lng);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Debug", "onStatusChanged");
            }

            public void onProviderEnabled(String provider) {
                Log.d("Debug", "onProviderEnabled");
            }

            public void onProviderDisabled(String provider) {
                Log.d("Debug", "onProviderDisabled");
            }
        };

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //사용자의 현재 위치 구하기
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        //연결하기 버튼 클릭 시 행동
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //server로 위도, 경도 전송
                    data.put("latitude",lat);
                    data.put("longitude",lng);
                    Log.d("Debug","lat:"+lat+"lng:"+lng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("login",data);
                socket.emit("connect 100");
                //startButton을 클릭할 경우 서버로 "login" emit
                connectDialog();
                //상대방과 연결시켜주는 다이얼로그를 띄워주는 메서드
            }
        });
    }

    //연결에 성공하였을 경우 실행
    private Emitter.Listener chatStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("Debug", "chatStart");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //ChatActivity로 이동
                    Intent intent = new Intent(ConnectActivity.this, ChatActivity.class);
                    startActivity(intent);
                }
            });

        }
    };

    //연결에 실패하였을 경우 실행
    private Emitter.Listener connectFail = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d("Debug", "connectFail");
            runOnUiThread(new Runnable() {
                JSONObject data = (JSONObject) args[0];
                @Override
                public void run() {
                    try {
                        //Server로 부터 요청한 거리 받기
                        distance = data.getDouble("distance");
                        Runnable mMyTask = new Runnable() {
                            @Override
                            public void run() {
                                //100m이내 연결 실패할 경우 200m이내로 다시 연결 하는 다이얼로그 띄워줌
                                if(distance == 0.1) {
                                    connectDialog.dismiss();
                                    connectAgain200Dialog();
                                }
                                //200m이내 연결 실패할 경우 400m이내로 다시 연결 하는 다이얼로그 띄워줌
                                if(distance == 0.2) {
                                    connectAgain200Dialog.dismiss();
                                    connectAgain400Dialog();
                                }
                                //400m이내 연결 실패할 경우 연결 실패 다이얼로그 띄워줌
                                if(distance == 0.4) {
                                    connectAgain400Dialog.dismiss();
                                    connectFailDialog();
                                }
                            }
                        };
                        mHandler.postDelayed(mMyTask, 4000); //4초 후 실행
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

        }
    };
    // 100m 이내의 사람들을 찾아주는 Dialog
    private void connectDialog() {
        connectDialog = new ConnectDialog(ConnectActivity.this, cancelClickListener);
        connectDialog.setCancelable(true);
        connectDialog.getWindow().setGravity(Gravity.CENTER);
        connectDialog.show();
        socket.on("connect fail", connectFail);
        Log.d("Debug", "connectDialog");
    }
    //cancel버튼 클릭하였을 경우 Server로 pop queue요청 및 다이얼로그 종료
    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d("Debug", "click cancel");
            socket.emit("pop queue");
            connectDialog.dismiss();
        }
    };

    //200m 이내의 사람들을 찾아주는 Dialog
    private void connectAgain200Dialog() {
        connectAgain200Dialog = new ConnectAgainDialog(ConnectActivity.this, "100", "200", againCancelClickListener, connectAgain200ClickListener);
        connectAgain200Dialog.setCancelable(true);
        connectAgain200Dialog.getWindow().setGravity(Gravity.CENTER);
        connectAgain200Dialog.show();
        Log.d("Debug", "connectAgain100Dialog");
    }

    //cancel버튼을 클릭하였을 경우 Server로 pop queue요청 및 다이얼로그 종료
    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectAgain200Dialog.dismiss();
            Log.d("Debug", "click cancel");
        }
    };

    //200m연결 버튼을 클릭하였을 경우 Server로 connect 200요청
    private View.OnClickListener connectAgain200ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Debug", "click button");
            connectAgain200Dialog.connectAgainProgressBar.setVisibility(View.VISIBLE);
            socket.emit("connect 200");

        }
    };

    //400m 이내의 사람들을 찾아주는 Dialog
    private void connectAgain400Dialog() {
        connectAgain400Dialog = new ConnectAgainDialog(ConnectActivity.this, "200", "400", againCancel400ClickListener, connectAgain400ClickListener);
        connectAgain400Dialog.setCancelable(true);
        connectAgain400Dialog.getWindow().setGravity(Gravity.CENTER);
        connectAgain400Dialog.show();

        Log.d("Debug", "connectAgain200Dialog");
    }

    //cancel버튼을 클릭하였을 경우 Server로 pop queue요청 및 다이얼로그 종료
    private View.OnClickListener againCancel400ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectAgain400Dialog.dismiss();
        }
    };

    //400m연결 버튼을 클릭하였을 경우 Server로 connect 400요청
    private View.OnClickListener connectAgain400ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Debug", "click button");
            connectAgain400Dialog.connectAgainProgressBar.setVisibility(View.VISIBLE);
            socket.emit("connect 400");
        }


    };

    //연결 실패 Dialog
    private void connectFailDialog() {
        connectFailDialog = new ConnectFailDialog(ConnectActivity.this, failCancelClickListener);
        connectFailDialog.setCancelable(true);
        connectFailDialog.getWindow().setGravity(Gravity.CENTER);
        connectFailDialog.show();
        Log.d("Debug", "connectFailDialog");
    }

    //calcel버튼을 클릭하였을 경우 Server로 pop queue요청 및 다이얼로그 종료
    private View.OnClickListener failCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectFailDialog.dismiss();
        }
    };

}
