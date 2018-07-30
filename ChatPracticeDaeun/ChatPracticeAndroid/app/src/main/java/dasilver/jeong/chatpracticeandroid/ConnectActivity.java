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
    private ConnectAgainDialog connectAgain100Dialog, connectAgain200Dialog;
    private Handler mHandler;
    private LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkEnabled;
    private JSONObject data;
    private double lat, lng;

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
        socket.on("chat start", chatStart);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }

        Log.d("Debug", "isGPSEnabled=" + isGPSEnabled);
        Log.d("Debug", "isNetworkEnabled=" + isNetworkEnabled);


        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();

                Log.d("Debug", "latitude: " + lat + ", longitude: " + lng);
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    data.put("latitude",lat);
                    data.put("longitude",lng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("login",data);
                //startButton을 클릭할 경우 서버로 "login" emit
                connectDialog();
                //상대방과 연결시켜주는 다이얼로그를 띄워주는 메서드
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
        Log.d("Debug", "connectDialog");

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
            Log.d("Debug", "click cancel");
            socket.emit("pop queue");
            connectDialog.dismiss();
        }
    };


    private void connectAgain100Dialog() {
        connectAgain100Dialog = new ConnectAgainDialog(ConnectActivity.this, "100", "200", againCancelClickListener, connectAgain200ClickListener);
        connectAgain100Dialog.setCancelable(true);
        connectAgain100Dialog.getWindow().setGravity(Gravity.CENTER);
        connectAgain100Dialog.show();
        Log.d("Debug", "connectAgain100Dialog");


    }

    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectAgain100Dialog.dismiss();
            Log.d("Debug", "click cancel");
        }
    };

    private View.OnClickListener connectAgain200ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Debug", "click button");
            connectAgain100Dialog.connectAgainProgressBar.setVisibility(View.VISIBLE);

            Runnable mMyTask = new Runnable() {
                @Override
                public void run() {
                    socket.emit("pop queue");
                    connectAgain100Dialog.dismiss();
                    connectAgain200Dialog();
                    Log.d("Debug", "click button");
                }
            };
            mHandler.postDelayed(mMyTask, 5000);//test 추후에 10000으로 바꿀 예정
        }
    };

    private void connectAgain200Dialog() {
        connectAgain200Dialog = new ConnectAgainDialog(ConnectActivity.this, "200", "400", againCancel200ClickListener, connectAgain400ClickListener);
        connectAgain200Dialog.setCancelable(true);
        connectAgain200Dialog.getWindow().setGravity(Gravity.CENTER);
        connectAgain200Dialog.show();
        Log.d("Debug", "connectAgain200Dialog");
    }

    private View.OnClickListener againCancel200ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectAgain200Dialog.dismiss();
        }
    };

    private View.OnClickListener connectAgain400ClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            Log.d("Debug", "click button");
            connectAgain200Dialog.connectAgainProgressBar.setVisibility(View.VISIBLE);
            Runnable mMyTask = new Runnable() {
                @Override
                public void run() {
                    socket.emit("pop queue");
                    connectAgain200Dialog.dismiss();
                    connectFailDialog();
                    Log.d("Debug", "click button");
                }
            };
            mHandler.postDelayed(mMyTask, 5000);//test 추후에 10000으로 바꿀 예정
        }


    };

    private void connectFailDialog() {
        connectFailDialog = new ConnectFailDialog(ConnectActivity.this, failCancelClickListener);
        connectFailDialog.setCancelable(true);
        connectFailDialog.getWindow().setGravity(Gravity.CENTER);
        connectFailDialog.show();
        Log.d("Debug", "connectFailDialog");
    }

    private View.OnClickListener failCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            socket.emit("pop queue");
            connectFailDialog.dismiss();
        }
    };

}
