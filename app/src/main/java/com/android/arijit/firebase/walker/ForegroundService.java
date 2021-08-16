package com.android.arijit.firebase.walker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static com.android.arijit.firebase.walker.SettingsFragment.distanceFormat;

public class ForegroundService extends Service {
    /**
     * data members
     */
    private String TAG = "ForegroundService";
    private Handler handler = new Handler();
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private FusedLocationProviderClient providerClient;
    private float totDistTravelled;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;
    public static MutableLiveData<Float> distInMetre = new MutableLiveData<>();
    public static MutableLiveData<ArrayList<LatLng>> curGotPosition = new MutableLiveData<>();
    public static ResultData result;


    /**
     * methods
     */

    private void initialize(){
        totDistTravelled = 0.00f;
        distInMetre.setValue(totDistTravelled);
        curGotPosition.setValue(new ArrayList<>());
        providerClient = LocationServices.getFusedLocationProviderClient(this);
        mNotificationManager = getSystemService(NotificationManager.class);
        result = new ResultData();
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initialize();
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setColor(getResources().getColor(R.color.green_300))
                .setSmallIcon(R.drawable.ic_baseline_directions_24)
                .setContentTitle("Tracking in progress...")
                .setContentText(distanceFormat(totDistTravelled))
                .setContentIntent(pendingIntent);

        startForeground(1, mNotificationBuilder.build());
        startTrack();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        result = null;
        stopTrack();
        handler.removeCallbacksAndMessages(null);
        getSystemService(NotificationManager.class).cancel(1);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void startTrack(){
        /**
         * providerClient to provide location service
         */
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setSmallestDisplacement(3f);

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(ForegroundService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(ForegroundService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                providerClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        });

    }
    private void stopTrack(){
        providerClient.removeLocationUpdates(locationCallback);
    }


    //=============
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            for (Location le:locationList){
                if(le == null)
                    continue;
                double lat = le.getLatitude(), lng = le.getLongitude();
                LatLng pos = new LatLng(lat, lng);
                Log.i(TAG, "service onLocationResult: "+lat+"+"+lng);

                if(curGotPosition.getValue().isEmpty()){
                    ArrayList<LatLng> tmp = curGotPosition.getValue();
                    tmp.add(pos);
                    curGotPosition.postValue(tmp);
                }
                else{
                    LatLng lastCoor = curGotPosition.getValue().get(curGotPosition.getValue().size() - 1);
                    Location lastLoc = new Location("");
                    lastLoc.setLatitude(lastCoor.latitude); lastLoc.setLongitude(lastCoor.longitude);

                    float dist = le.distanceTo(lastLoc);
                    if(dist < 3f) {
                        continue;
                    }
                    totDistTravelled += dist;
                    ArrayList<LatLng> tmp = curGotPosition.getValue();
                    tmp.add(pos);
                    curGotPosition.postValue(tmp);
                }
                Log.i(TAG, "onLocationResult: mutSize "+curGotPosition.getValue().size());
                mNotificationBuilder.setContentText(distanceFormat(totDistTravelled));
                mNotificationManager.notify(1, mNotificationBuilder.build());
                distInMetre.postValue(totDistTravelled);
            }
        }
    };


}
