package com.android.arijit.firebase.walker.views;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.adapters.ResultDataAdapter;
import com.android.arijit.firebase.walker.databinding.FragmentHistoryBinding;
import com.android.arijit.firebase.walker.interfaces.OnDataFetchedListener;
import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.interfaces.OnHistoryItemClickedListener;
import com.android.arijit.firebase.walker.models.ResultData;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;
import com.android.arijit.firebase.walker.utils.ViewUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;


public class HistoryFragment extends Fragment implements OnMapReadyCallback,
        OnHistoryItemClickedListener, OnDataFetchedListener {

    /**
     * data members
     */

    private ArrayList<ResultData> resultDataArrayList;
    private Animation mapPopupReveal;
    private GoogleMap mMap;
    private FragmentHistoryBinding binding;
    private OnFirebaseResultListener firebaseResultListener;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public HistoryFragment(OnFirebaseResultListener listener1){
        this.firebaseResultListener = listener1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleBackPress();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        initialize(savedInstanceState);
        setObservers();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUtil.fetchData(requireContext(), this);
    }

    private void handleBackPress() {
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // if map is shown hide map
                if(binding.mapPopupContainer.getRoot().getVisibility()==View.VISIBLE){
                    mMap.clear();
                    binding.mapPopupContainer.getRoot().setVisibility(View.GONE);
                }
                else {
                    ((BottomNavigationView)requireActivity().findViewById(R.id.navigation))
                            .setSelectedItemId(R.id.navigation_home);
                }
            }
        });
    }

    @Override
    public void onDataFetched(@Nullable String result) {
        binding.loading.setVisibility(View.GONE);
        firebaseResultListener.onFirebaseResult(result);
    }

    private void initialize(Bundle savedInstanceState) {
        mapPopupReveal = AnimationUtils.loadAnimation(requireContext(), R.anim.map_popup_reveal);
        resultDataArrayList = new ArrayList<>();
        binding.recView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.mapPopupContainer.historyMapView.onCreate(savedInstanceState);
        binding.mapPopupContainer.historyMapView.getMapAsync(this);
    }

    private void setObservers() {
        FirebaseUtil.liveResultData.observe(requireActivity(), resultData -> {
            resultDataArrayList = resultData;
            binding.recView.setVisibility(View.VISIBLE);
            if(resultDataArrayList != null){
                ResultDataAdapter mAdapter = new ResultDataAdapter(getContext(),
                        resultDataArrayList, HistoryFragment.this, this.firebaseResultListener);
                binding.recView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onHistoryItemClicked(int position) {
        binding.mapPopupContainer.getRoot();
        if(binding.mapPopupContainer.getRoot().getVisibility() == View.GONE){
            new Handler().post(()->{
                binding.mapPopupContainer.getRoot().setVisibility(View.VISIBLE);
                binding.mapPopupContainer.getRoot().startAnimation(mapPopupReveal);
            });
            ArrayList<LatLng> travelCoor = resultDataArrayList
                    .get(position).getTravelCoordinates();
            if(travelCoor.size() <= 0) return;
            LatLng end = travelCoor.get(travelCoor.size() - 1);
            LatLng stt = travelCoor.get(0);
            //end marker
            mMap.addMarker(new MarkerOptions().position(end).title(getString(R.string.end)));
            //start marker
            mMap.addMarker(new MarkerOptions().position(stt).title(getString(R.string.start)));

            mMap.addPolyline(new PolylineOptions().color(HomeFragment.POLYLINE_COLOR).addAll(travelCoor));
            mMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                            CameraPosition.builder()
                                .target(end)
                                .zoom(SettingsFragment.DEFAULT_ZOOM)
                                .build()
                    )
            );
        }

    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        ViewUtil.setMapTheme(requireContext(), mMap);

        mMap.setMaxZoomPreference(18);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapPopupContainer.historyMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapPopupContainer.historyMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapPopupContainer.historyMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapPopupContainer.historyMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapPopupContainer.historyMapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapPopupContainer.historyMapView.onDestroy();
    }
}