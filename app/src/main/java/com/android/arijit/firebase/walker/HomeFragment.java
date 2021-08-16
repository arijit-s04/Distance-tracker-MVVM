package com.android.arijit.firebase.walker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorLong;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static String TAG = "HomeFragment";

    public HomeFragment() {
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        ForegroundService.distInMetre.observe(this, this.mDistObserver);
        ForegroundService.curGotPosition.observe(this, this.mTravelCoordinateObserver);
    }

    /**
     * Data members
     */

    private MapView mapView;
    private GoogleMap mMap;
    private Animation disReveal, disHide, countDownZoom, cardReveal, cardHide;
    private FloatingActionButton fabAction, fabCurLocation;
    private TextView tvDistance, tvTimer, resultDate, resultTime, resultDistance;
    private CardView tvContainer;
    private FusedLocationProviderClient providerClient;
    public static String[] wantedPerm = {Manifest.permission.ACCESS_FINE_LOCATION};
    private CameraPosition.Builder cameraBuilder;
    private Marker curMarker;
    private PolylineOptions polylineOptions;
    public static boolean trackState = false;
    private ArrayList<LatLng> travelCoordinates;
    private LatLng initLatLng;
    private Button btnCardOk;
    private ResultData resultToStore;
    private View resultContainer;
    public static int POLYLINE_COLOR;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        initialise(root);
        if(savedInstanceState != null){
            initLatLng = (LatLng) savedInstanceState.getParcelable("initlatlng");
        }

        if (!trackState) {
            tvContainer.setVisibility(View.INVISIBLE);
        } else {
            tvDistance.setText(
                    SettingsFragment.distanceFormat(ForegroundService.distInMetre.getValue())
            );
        }

        /**
         * init map
         */
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        /**
         * listener
         */

        fabAction.setOnClickListener(v -> {
            if (!trackState) {
                countDownStart();
            } else {
                trackState = false;
                tvContainer.startAnimation(disHide);
                getResult();
                disHide.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        tvContainer.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                stopService();
            }
        });

        fabCurLocation.setOnClickListener(v -> {
            setCurrentLocation();
        });

        btnCardOk.setOnClickListener(v -> {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    resultContainer.startAnimation(cardHide);
                    cardHide.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            (root.findViewById(R.id.container_result)).setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mMap.clear();
                            if(curMarker != null){
                                curMarker = mMap.addMarker(new MarkerOptions().position(curMarker.getPosition()));
                            }
                            setCurrentLocation();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                }
            });
        });

        return root;
    }

    private void initialise(View root) {
        mapView = root.findViewById(R.id.mapView);
        /**
         * check location enabled
         */
        isLocationEnabled();
        cameraBuilder = new CameraPosition.Builder()
                .zoom(SettingsFragment.CAMERA_ZOOM)
                .tilt(SettingsFragment.CAMERA_TILT)
                .bearing(SettingsFragment.CAMERA_BEARING);
        disReveal = AnimationUtils.loadAnimation(getContext(), R.anim.distance_reveal);
        disHide = AnimationUtils.loadAnimation(getContext(), R.anim.distance_hide);
        cardReveal = AnimationUtils.loadAnimation(getContext(), R.anim.result_card_reveal);
        cardHide = AnimationUtils.loadAnimation(getContext(), R.anim.result_card_hide);
        countDownZoom = AnimationUtils.loadAnimation(getContext(), R.anim.count_down_zoom);
        fabAction = root.findViewById(R.id.fab_action);
        fabCurLocation = root.findViewById(R.id.fab_cur_location);
        tvDistance = root.findViewById(R.id.tv_distance);
        tvTimer = root.findViewById(R.id.tv_timer);
        tvContainer = root.findViewById(R.id.tv_container);
        travelCoordinates = new ArrayList<>();
        providerClient = LocationServices.getFusedLocationProviderClient(getContext());
        btnCardOk = root.findViewById(R.id.result_btn_ok);
        resultContainer = root.findViewById(R.id.container_result);
        resultDate = root.findViewById(R.id.result_date);
        resultTime = root.findViewById(R.id.result_time);
        resultDistance = root.findViewById(R.id.result_distance);
    }

    /**
     * map Ready
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        setMapTheme();

        mMap.setMaxZoomPreference(18);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        polylineOptions = new PolylineOptions().color(POLYLINE_COLOR);

        if (!trackState && initLatLng != null) {
            cameraBuilder.target(initLatLng);
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            cameraBuilder.bearing(SettingsFragment.CAMERA_BEARING);
            cameraBuilder.tilt(SettingsFragment.CAMERA_TILT);

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            curMarker = mMap.addMarker(new MarkerOptions()
                    .position(initLatLng));
            return;
        } else if (trackState) {
            travelCoordinates = ForegroundService.curGotPosition.getValue();
            if (travelCoordinates != null && travelCoordinates.size() > 0) {
                curMarker = mMap.addMarker(new MarkerOptions()
                        .position(travelCoordinates.get(travelCoordinates.size() - 1))
                );
                for (LatLng l : travelCoordinates) {
                    polylineOptions.add(l);
                    mMap.addPolyline(polylineOptions);
                }
                initLatLng = travelCoordinates.get(travelCoordinates.size() - 1);
                cameraBuilder.target(travelCoordinates.get(travelCoordinates.size() - 1));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            }

            return;
        }

        setCurrentLocation();

    }

    private void setMapTheme(){
        int res,
                nightModeFlag = getContext().getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlag){
            case Configuration.UI_MODE_NIGHT_YES:
                res = R.raw.style_json_night;
                POLYLINE_COLOR = Color.WHITE;
                break;
            default:
                POLYLINE_COLOR = Color.BLACK;
                res = R.raw.style_json;
                break;
        }
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), res));
            if (!success) {
                Log.i(TAG, "onMapReady: parse failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.i(TAG, "onMapReady: style not found");
        }
    }

    Observer<Float> mDistObserver = f -> {
        if (!trackState)
            return;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                tvDistance.setText(SettingsFragment.distanceFormat(f));
            }
        });

    };

    Observer<ArrayList<LatLng>> mTravelCoordinateObserver = list -> {
        if (!trackState | list.isEmpty()) return;

        travelCoordinates = list;
        initLatLng = travelCoordinates.get(travelCoordinates.size() - 1);
        if (mMap != null && curMarker != null) {
            MarkerAnimation.animateMarkerToGB(mMap, curMarker, initLatLng, new LatLngInterpolator.Spherical());
        }
    };

    private void setCurrentLocation() {
        if (!trackState) {

            new Thread(() -> {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), wantedPerm, 101);
                }
                providerClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                    @Override
                    public boolean isCancellationRequested() {
                        return false;
                    }

                    @NonNull
                    @Override
                    public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                        return null;
                    }
                })
                .addOnSuccessListener(location -> {
                    if (location == null)
                        return;
                    initLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (curMarker == null) {
                        Log.i(TAG, "onSuccess: null true");
                        curMarker = mMap.addMarker(new MarkerOptions()
                                .position(initLatLng));
                    } else {
                        Log.i(TAG, "onSuccess: null false " + (mMap == null));
                        curMarker.setPosition(initLatLng);
                    }
                    cameraBuilder.zoom(16);
                    cameraBuilder.target(initLatLng);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));

                })
                .addOnFailureListener(e -> {
                    Snackbar.make(mapView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }).start();
        } else if (travelCoordinates != null && travelCoordinates.size() > 0) {
            cameraBuilder.target(travelCoordinates.get(travelCoordinates.size() - 1));
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            SettingsFragment.CAMERA_ZOOM = mMap.getCameraPosition().zoom;
            SettingsFragment.CAMERA_TILT = mMap.getCameraPosition().tilt;
            SettingsFragment.CAMERA_BEARING = mMap.getCameraPosition().bearing;
        }
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        outstate.putParcelable("initlatlng", initLatLng);
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ForegroundService.distInMetre.removeObserver(this.mDistObserver);
        ForegroundService.curGotPosition.removeObserver(this.mTravelCoordinateObserver);
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void isLocationEnabled() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, netEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
        }
        try {
            netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
        }
        if (!gpsEnabled && !netEnabled) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.gps_not_enabled)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(getContext(), ForegroundService.class);
        getActivity().stopService(serviceIntent);
    }


    private void countDownStart() {
        //==========
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        providerClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
        //==========
        final int[] time = {3};
        tvTimer.setVisibility(View.VISIBLE);
        Handler countDown = new Handler();
        countDown.post(new Runnable() {
            @Override
            public void run() {
                if(time[0] >0){
                    if(fabAction.isEnabled())   fabAction.setEnabled(false);
                    tvTimer.setText(String.valueOf(time[0]));
                    tvTimer.startAnimation(countDownZoom);
                    time[0]--;
                    countDown.postDelayed(this, 1000);
                }
                else{
                    if(!fabAction.isEnabled())   fabAction.setEnabled(true);
                    providerClient.removeLocationUpdates(mLocationCallback);
                    tvTimer.setVisibility(View.GONE);
                    trackState = true;
                    tvContainer.startAnimation(disReveal);
                    disReveal.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            tvContainer.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), wantedPerm, 101);
                    }
                    startService();
                }
            }
        });
    }

    final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locationList = locationResult.getLocations();
            if (!locationList.isEmpty()) {
                initLatLng = new LatLng(locationList.get(locationList.size() - 1).getLatitude(),
                        locationList.get(locationList.size() - 1).getLongitude());
                if(curMarker != null)
                    curMarker.setPosition(initLatLng);
            }
        }
    };
    private void getResult(){
        this.resultToStore = ForegroundService.result;
        this.resultToStore.setDistanceTravelled(ForegroundService.distInMetre.getValue());
        this.resultToStore.setTravelCoordinates(this.travelCoordinates);
        // TODO: 12/06/21 add the firebase method to upload the data
        Log.i(TAG, "getResult: "+resultToStore.getTime()+" "+resultToStore.getDistanceTravelled());
        FirebaseHelper.storeData(resultToStore, mapView);
        /**
         * add firebase above this
         */
        new Handler().post(() -> {
            resultDistance.setText(SettingsFragment.distanceFormat(resultToStore.getDistanceTravelled()));
            resultDate.setText(resultToStore.getDate());
            resultTime.setText(resultToStore.getTime());
            resultContainer.setVisibility(View.VISIBLE);
            resultContainer.startAnimation(cardReveal);
        });
    }
}