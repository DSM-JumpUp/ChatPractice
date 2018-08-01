package com.jumpup.tails.studysocket;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jumpup.tails.studysocket.gps.GPSInfo;
import com.jumpup.tails.studysocket.dialog.LoginWaitDialog;
import com.jumpup.tails.studysocket.socket.SocketApplication;

import java.util.List;
import java.util.Random;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.jumpup.tails.studysocket.socket.SocketApplication.LOGIN_SUCCESS;

public class ConnectActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private Socket mSocket;
    private LoginWaitDialog dialogFragment;
    private GPSInfo gpsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        PermissionCheck();
        gpsInfo = new GPSInfo(ConnectActivity.this);

        Button btn = findViewById(R.id.btn_connect_start);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryMatching();
            }
        });

        mSocket = SocketApplication.getSocket();
        mSocket.on("chat start", onChatStart);
    }

    private String getUsername() {
        Resources res = getResources();
        String[] names = res.getStringArray(R.array.names_array);
        Random rnd = new Random();
        rnd.nextInt();
        return names[rnd.nextInt(names.length - 1)];
    }

    private void tryMatching() {
        if(gpsInfo.getLocation() != null){
            Log.e("GPS", gpsInfo.getLatitude() + " " + gpsInfo.getLongitude());
            mSocket.emit("add user", gpsInfo.getLatitude(), gpsInfo.getLongitude());

            FragmentManager fm = getSupportFragmentManager();
            dialogFragment = new LoginWaitDialog();
            dialogFragment.setCancelable(false);
            dialogFragment.show(fm, "wait_fragment_dialog");
        }else {
            gpsInfo.showSettingsAlert();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        gpsInfo.stopUsingGPS();
        mSocket.off("chat start", onChatStart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.off("chat start", onChatStart);
        gpsInfo.stopUsingGPS();
    }

    private Emitter.Listener onChatStart = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() { dialogFragment.dismiss(); }
            });
            Intent sendIntent = new Intent(getApplicationContext(), MainActivity.class);
            sendIntent.putExtra("userName", getUsername());
            setResult(LOGIN_SUCCESS, sendIntent);
            finish();
        }
    };

    void PermissionCheck(){
        if(!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_COARSE_LOCATION)){
            EasyPermissions.requestPermissions(this, "앱에 필요한 권한을 부여해야 합니다!",
                    0, Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) { }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }
}
