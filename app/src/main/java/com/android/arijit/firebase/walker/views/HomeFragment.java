package com.android.arijit.firebase.walker.views;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.applications.App;
import com.android.arijit.firebase.walker.databinding.FragmentHomeBinding;
import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.models.ForegroundService;
import com.android.arijit.firebase.walker.utils.LatLngInterpolator;
import com.android.arijit.firebase.walker.utils.MarkerAnimation;
import com.android.arijit.firebase.walker.utils.ViewUtil;
import com.android.arijit.firebase.walker.viewmodel.HistoryListViewModel;
import com.android.arijit.firebase.walker.viewmodel.LocationViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    public static String TAG = "HomeFragment";
    private OnFirebaseResultListener firebaseResultListener;
    private ValueAnimator valueAnimator;

    public HomeFragment() {
    }

    public HomeFragment(OnFirebaseResultListener listener) {
        this.firebaseResultListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        viewModel = ForegroundService.locationViewModel;
        setObservers();
    }

    /**
     * Data members
     */
    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private Animation disReveal, disHide, countDownZoom, cardReveal, cardHide;
    private FusedLocationProviderClient providerClient;
    public static String[] wantedPerm = {Manifest.permission.ACCESS_FINE_LOCATION};
    private CameraPosition.Builder cameraBuilder;
    private Marker curMarker;
    private ArrayList<LatLng> travelCoordinates;
    private LatLng initLatLng;
    public static int POLYLINE_COLOR;
    private LocationViewModel viewModel;
    private HistoryListViewModel historyViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.containerResult.setViewModel(viewModel);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialise();
        if(savedInstanceState != null) initLatLng = savedInstanceState.getParcelable("initlatlng");

        if (!viewModel.getTrackState()) {
            binding.tvContainer.setVisibility(View.INVISIBLE);
        }

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        historyViewModel = new ViewModelProvider(requireActivity()).get(HistoryListViewModel.class);
        historyViewModel.getHistoryLiveList(null);
        if(viewModel.getTrackState())
            binding.fabAction.setBackgroundTintList(ColorStateList.valueOf(App.getContext().getColor(R.color.stop_red)));
        valueAnimator = ViewUtil.animatorForFab(binding.fabAction);
        setOnClickListeners();
    }

    private void animateButton(boolean start) {
        if(start)
            valueAnimator.start();
        else
            valueAnimator.reverse();
    }


    private void setOnClickListeners() {
        binding.fabAction.setOnClickListener(v -> {
            if (!viewModel.getTrackState()) {
                animateButton(true);
                countDownStart();
            } else {
                animateButton(false);
                stopTracking();
            }
        });

        binding.fabCurLocation.setOnClickListener(v -> setCurrentLocation());

        binding.containerResult.resultBtnOk.setOnClickListener(v -> new Handler().post(() -> {
            binding.containerResult.getRoot().startAnimation(cardHide);
            cardHide.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    binding.containerResult.getRoot().setVisibility(View.GONE);
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
        }));
    }

    private void setObservers() {
        viewModel.getCurGotPosition().observe(this, list->{
            if (!viewModel.getTrackState() | list.isEmpty()) return;
            travelCoordinates = list;
            initLatLng = travelCoordinates.get(travelCoordinates.size() - 1);
            Log.i(TAG, "setObservers: init "+initLatLng);
            if (mMap != null && curMarker != null) {
                Log.i(TAG, "setObservers: inside if "+initLatLng);
                MarkerAnimation.animateMarkerToGB(mMap, curMarker, initLatLng, new LatLngInterpolator.Spherical());
            }
        });
    }

    private void initialise() {
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
        travelCoordinates = new ArrayList<>();
        providerClient = LocationServices.getFusedLocationProviderClient(requireContext());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        ViewUtil.setMapTheme(requireContext(), mMap);

        mMap.setMaxZoomPreference(18);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        PolylineOptions polylineOptions = new PolylineOptions().color(POLYLINE_COLOR);

        if (!viewModel.getTrackState() && initLatLng != null) {
            cameraBuilder.target(initLatLng);
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            cameraBuilder.bearing(SettingsFragment.CAMERA_BEARING);
            cameraBuilder.tilt(SettingsFragment.CAMERA_TILT);

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
            curMarker = mMap.addMarker(new MarkerOptions()
                    .position(initLatLng));
            return;
        }
        else if (viewModel.getTrackState()) {
            travelCoordinates = viewModel.getCurGotPosition().getValue();
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

    private void stopTracking() {
        viewModel.setTrackState(false);
        binding.tvContainer.startAnimation(disHide);
        stopService();
        getResult();
        disHide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.tvContainer.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

    }

    private void setCurrentLocation() {
        if (!viewModel.getTrackState()) {

            new Handler().post(() -> {
                if (ActivityCompat.checkSelfPermission(App.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(App.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), wantedPerm, 101);
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
                    cameraBuilder.zoom(SettingsFragment.DEFAULT_ZOOM);
                    cameraBuilder.target(initLatLng);
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));

                })
                .addOnFailureListener(e -> Snackbar.make(binding.mapView, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show());
            });
        } else if (travelCoordinates != null && travelCoordinates.size() > 0) {
            cameraBuilder.target(travelCoordinates.get(travelCoordinates.size() - 1));
            cameraBuilder.zoom(SettingsFragment.CAMERA_ZOOM);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraBuilder.build()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMap != null) {
            SettingsFragment.CAMERA_ZOOM = mMap.getCameraPosition().zoom;
            SettingsFragment.CAMERA_TILT = mMap.getCameraPosition().tilt;
            SettingsFragment.CAMERA_BEARING = mMap.getCameraPosition().bearing;
        }
        binding.mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outstate){
        outstate.putParcelable("initlatlng", initLatLng);
        super.onSaveInstanceState(outstate);
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        binding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    private void isLocationEnabled() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false, netEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {
        }
        try {
            netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {
        }
        if (!gpsEnabled && !netEnabled) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(R.string.gps_not_enabled)
                    .setPositiveButton(R.string.ok, (dialog, which) -> requireContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(requireContext(), ForegroundService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(requireContext(), ForegroundService.class);
        requireActivity().stopService(serviceIntent);
    }


    private void countDownStart() {
        //==========
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        providerClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
        //==========
        final int[] time = {SettingsFragment.DEFAULT_COUNT_DOWN};
        binding.tvTimer.setVisibility(View.VISIBLE);
        Handler countDown = new Handler();
        countDown.post(new Runnable() {
            @Override
            public void run() {
                if(time[0] >0){
                    if(binding.fabAction.isEnabled())   binding.fabAction.setEnabled(false);
                    binding.tvTimer.setText(String.valueOf(time[0]));
                    binding.tvTimer.startAnimation(countDownZoom);
                    time[0]--;
                    countDown.postDelayed(this, 1000);
                }
                else{
                    if(!binding.fabAction.isEnabled())   binding.fabAction.setEnabled(true);
                    providerClient.removeLocationUpdates(mLocationCallback);
                    binding.tvTimer.setVisibility(View.GONE);
                    viewModel.setTrackState(true);
                    binding.tvContainer.startAnimation(disReveal);
                    disReveal.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            binding.tvContainer.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), wantedPerm, 101);
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
        viewModel.saveResult(historyViewModel, this.firebaseResultListener);
        /*
         * add firebase above this
         */
        new Handler().post(() -> {
            binding.containerResult.getRoot().setVisibility(View.VISIBLE);
            binding.containerResult.getRoot().startAnimation(cardReveal);
        });
    }
}