package com.example.dsm2017.android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    SocketApp socketApp;
    Socket mSocket;
    private DialogConActivity dialogConActivity;
    private DialogFailureActivity dialogFailureActivity;
    private DialogAgainActivity dialogAgainActivity;
    private Handler mHandler;
    private int cannot, findAgain;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private double lat, lng;
    private int length;

    Button button;

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            if (hasPermissions()) {
//                Toast.makeText(getApplicationContext(), "이미 허용함.", Toast.LENGTH_LONG).show();
            } else {
                requestPerms();
            }
        } else {
//            Toast.makeText(getApplicationContext(), "이용할 수 없음.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GPS 설정화면으로 이동
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        }


        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lat = location.getLatitude();
                lng = location.getLongitude();

                Log.d("Debug", "latitude: " + lat + ", longitude: " + lng);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

                Log.d("Debug", "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("Debug", "onProviderEnabled");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Debug", "onProviderDisabled");
            }
        };

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

        socketApp = (SocketApp) getApplication();

        mSocket = socketApp.getSocket();
        mSocket.connect();

        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect timeout");
            }
        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("connect error");
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println("disconnect");
            }
        });


        button = findViewById(R.id.search_button);
        button.setOnClickListener(clickFind);

        mSocket.on("join", joinData);
        mSocket.on("exit", exitRoom);
}


    private Emitter.Listener joinData = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject JO = (JSONObject) args[0];

            try {
                final String peerName = JO.getString("name");
                final String roomName = JO.getString("roomName");

                Log.i("join", peerName + " : " + roomName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this, ChattingActivity.class);
                        intent.putExtra("peerName", peerName);
                        intent.putExtra("roomName", roomName);
                        dialogConActivity.dismiss();
                        mHandler.removeMessages(0);
                        startActivity(intent);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private View.OnClickListener clickFind = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            length = 100;

            try {
                ViewOnConDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            cannot = 100;
            findAgain = 200;
        }
    };

    private void ViewOnConDialog() throws JSONException {
        dialogConActivity = new DialogConActivity(MainActivity.this, cancelClickListener);
        dialogConActivity.setCancelable(true);
        dialogConActivity.getWindow().setGravity(Gravity.CENTER);
        dialogConActivity.show();
        mSocket.emit("search", setGPSData(lat, lng, length));

        Runnable TimeOver = new Runnable() {
            @Override
            public void run() {
                if (cannot <= 200 && findAgain <= 400) {
                    mSocket.emit("pop queue");
                    dialogConActivity.dismiss();
                    connectAgainDialog(cannot, findAgain);
                } else {
                    mSocket.emit("pop queue");
                    dialogConActivity.dismiss();
                    connectFailDialog();
                }
            }
        };
        mHandler.postDelayed(TimeOver, 10000);
    }

    private void connectAgainDialog(int cannotFind, int againFind) {
        dialogAgainActivity = new DialogAgainActivity(MainActivity.this, cannotFind, againFind, againCancelClickListener, againFindPeople);
        dialogAgainActivity.setCancelable(true);
        dialogAgainActivity.getWindow().setGravity(Gravity.CENTER);
        dialogAgainActivity.show();

        cannot = cannot * 2;
        findAgain = findAgain * 2;
    }

    private View.OnClickListener cancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSocket.emit("pop queue");
            dialogConActivity.dismiss();
            mHandler.removeMessages(0);
        }
    };

    private View.OnClickListener againCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mSocket.emit("pop queue");
            dialogAgainActivity.dismiss();
        }
    };

    private View.OnClickListener againFindPeople = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialogAgainActivity.dismiss();
            length = length * 2;
            try {
                ViewOnConDialog();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private void connectFailDialog() {
        dialogFailureActivity = new DialogFailureActivity(MainActivity.this, failCancelClickListener);
        dialogFailureActivity.setCancelable(true);
        dialogFailureActivity.getWindow().setGravity(Gravity.CENTER);
        dialogFailureActivity.show();
        mSocket.emit("pop queue");
    }

    private View.OnClickListener failCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogFailureActivity.dismiss();
        }
    };

    private boolean hasPermissions() {
        int res = 0;

        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        for (String perms: permissions){
            res = checkCallingOrSelfPermission(perms);

            if(!(res == PackageManager.PERMISSION_GRANTED))
                return false;
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case 0:
                for (int res: grantResults) {
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }
                break;

            default:
                allowed = false;
                break;
        }
        if(!allowed) {
            Toast.makeText(getApplicationContext(), "권한이 거부되었습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private JSONObject setGPSData(double lat, double lng, int length) throws JSONException {
        JSONObject GPSData = new JSONObject();

        GPSData.put("lat", lat);
        GPSData.put("lng", lng);
        GPSData.put("length", length);

        Log.d("GPSData", "lat : " + lat + " ,lng" + lng );

        return GPSData;
    }
    private Emitter.Listener exitRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), getResources().getText(R.string.peerExitRoom), Toast.LENGTH_SHORT).show();
                }
            });
        };
    };
}
