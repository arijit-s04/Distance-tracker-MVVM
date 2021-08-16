package com.android.arijit.firebase.walker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        FirebaseHelper.liveResultData.observe(this, this.resultDataListObserver);
        mapToShow.setValue(Boolean.FALSE);

        /**
         * show the map on clicking a record
         */
        mapToShow.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean == false)
                    return;
                if(mapPopupContainer!=null && mapPopupContainer.getVisibility()==View.GONE){
                    new Handler().post(()->{
                        mapPopupContainer.setVisibility(View.VISIBLE);
                        mapPopupContainer.startAnimation(mapPopupReveal);
                    });
                    ArrayList<LatLng> travelCoor = resultDataArrayList
                            .get(clickedPosition).getTravelCoordinates();
                    LatLng end = travelCoor.get(travelCoor.size() - 1);
                    LatLng stt = travelCoor.get(0);
                    //end marker
                    mMap.addMarker(new MarkerOptions().position(end).title("End"));
                    //start marker
                    mMap.addMarker(new MarkerOptions().position(stt).title("Start"));

                    mMap.addPolyline(new PolylineOptions().color(HomeFragment.POLYLINE_COLOR).addAll(travelCoor));
                    mMap.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                    CameraPosition.builder()
                                            .target(end)
                                            .zoom(16)
                                            .build()
                            )
                    );
                }
            }
        });
        /**
         * backPress handler in fragment
         */
        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(mapPopupContainer.getVisibility()==View.VISIBLE){
                    mMap.clear();
                    mapPopupContainer.setVisibility(View.GONE);
                    mapToShow.setValue(false);
                }
                else {
                    ((BottomNavigationView)getActivity().findViewById(R.id.navigation))
                            .setSelectedItemId(R.id.navigation_home);
                }
            }
        });

    }

    private View mapPopupContainer;
    private RecyclerView recyclerView;
    private ArrayList<ResultData> resultDataArrayList;
    private String TAG = "HistoryFragment";
    public static MutableLiveData<Boolean> mapToShow = new MutableLiveData<>();
    private Animation mapPopupReveal;
    private MapView mapView;
    private GoogleMap mMap;
    public static int clickedPosition;
    private ProgressBar loading;
    /**
     * data members
     */

    Observer<ArrayList<ResultData>> resultDataListObserver = new Observer<ArrayList<ResultData>>() {
        @Override
        public void onChanged(ArrayList<ResultData> resultData) {
//            loading.setVisibility(View.GONE);
            resultDataArrayList = resultData;
            recyclerView.setVisibility(View.VISIBLE);
            if(recyclerView !=null && resultDataArrayList!= null){
                ResultDataAdapter mAdapter = new ResultDataAdapter(getContext(), getView(), resultDataArrayList);
                recyclerView.setAdapter(mAdapter);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        /**
         * init
         */
        loading = (ProgressBar) root.findViewById(R.id.loading);
        mapPopupReveal = AnimationUtils.loadAnimation(getContext(), R.anim.map_popup_reveal);
        mapPopupContainer = root.findViewById(R.id.map_popup_container);
        resultDataArrayList = new ArrayList<>();
        recyclerView=root.findViewById(R.id.recView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mapView = root.findViewById(R.id.history_map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::onMapReady);

        FirebaseHelper.fetchData(loading);

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap = googleMap;
        setMapTheme();

        mMap.setMaxZoomPreference(18);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void setMapTheme(){
        int res,
                nightModeFlag = getContext().getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlag){
            case Configuration.UI_MODE_NIGHT_YES:
                res = R.raw.style_json_night;
                break;
            default:
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
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}