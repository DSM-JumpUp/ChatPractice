package com.jumpup.tails.studysocket.gps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class GPSInfo extends Service implements LocationListener {
    private final Context mContext;

    public boolean isGPSEnabled = false;
    public boolean isNetworkEnabled = false;

    Location location;
    double lat;
    double lon;


    // 최소 GPS 정보 업데이트 거리 10미터
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;

    // 최소 GPS 정보 업데이트 시간 밀리세컨(1분)
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    protected LocationManager locationManager;


    public GPSInfo(Context mContext) {
        this.mContext = mContext;
    }

    @TargetApi(23)
    public Location getLocation(){
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return null;
        }

        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            assert locationManager != null;

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.e("Not Enabled!!1", "ASdfdsfsd");
                return null;
            }else{
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if(locationManager != null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.e("location NETWORK", String.valueOf(location == null));

                    if(location != null){
                        lat = location.getLatitude();
                        lon = location.getAltitude();
                    }

                    if(isGPSEnabled){
                        if(location == null){
                            locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                            if(locationManager != null) {
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                Log.e("location GPS_PROVIDER", String.valueOf(location == null));

                                if (location != null) {
                                    lat = location.getLatitude();
                                    lon = location.getAltitude();
                                }
                            }
                        }
                    }
                }else{
                    Log.e("locationManager", "is NULLL!!!!!!!!!!!!!");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSInfo.this);
        }
    }

    public double getLatitude(){
        if (location != null){
            lat = location.getLatitude();
        }
        return lat;
    }

    public double getLongitude(){
        if(location != null){
            lon = location.getLongitude();
        }
        return lon;
    }

    public void showSettingsAlert(){
        makeDialog();
    }

    private void makeDialog() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);

        alt_bld.setMessage("GPS 사용이 필요합니다.\n설정창으로 가시겠습니까?").setCancelable(
                false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        mContext.startActivity(intent);
                    }
                }).setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alt_bld.create();
        alertDialog.show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onLocationChanged(Location location) { }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
}
