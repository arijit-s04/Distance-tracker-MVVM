package com.android.arijit.firebase.walker.utils;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.interfaces.OnDataFetchedListener;
import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.models.ResultData;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    public static MutableLiveData<ArrayList<ResultData>> liveResultData = new MutableLiveData<>();
    final static String DATE = "date",
            TIME = "time",
            DISTANCE = "distance",
            TRAVEL_COORDINATES = "travel_coordinates",
            SERVER_TIME = "server_time",
            HISTORY_COLLECTION = "travel_history",
            USER = "user";

    public static void storeData(Context context, ResultData data, OnFirebaseResultListener listener){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String email ;
        try{
            email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        } catch (Exception e){
            email = "email";
        }

        HashMap<String, Object> toPut = new HashMap<>();
        toPut.put(USER, email);
        toPut.put(DATE, data.getDate());
        toPut.put(TIME, data.getTime());
        toPut.put(DISTANCE, data.getDistanceTravelled());
        toPut.put(TRAVEL_COORDINATES, data.getTravelCoordinates());
        toPut.put(SERVER_TIME, FieldValue.serverTimestamp());

        mFirestore.collection(HISTORY_COLLECTION)
                .add(toPut)
                .addOnFailureListener(error -> {
                    listener.onFirebaseResult(context.getString(R.string.error_save));
                    Log.i(TAG, "storeData: failure "+ error.getMessage());
                });
    }

    @SuppressWarnings("unchecked")
    public static void fetchData(final Context context, final OnDataFetchedListener listener){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String email ;
        try{
            email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        } catch (Exception e){
            email = "email";
        }
        ArrayList<ResultData> fetchedData = new ArrayList<>();
        liveResultData.setValue(fetchedData);

        mFirestore.collection(HISTORY_COLLECTION)
                .orderBy(SERVER_TIME)
                .whereEqualTo(USER, email)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isComplete()){
                        Log.i(TAG, "onComplete: "+task.getResult().size());
                        for (QueryDocumentSnapshot docSnap : task.getResult()){
                            ResultData res = new ResultData();
                            res.setId(docSnap.getId());
                            res.setDate(docSnap.getString(DATE));
                            res.setTime(docSnap.getString(TIME));
                            res.setDistanceTravelled(Float.parseFloat(Objects.requireNonNull(docSnap.get(DISTANCE)).toString()));
                            ArrayList<Object> tmp = (ArrayList<Object>) docSnap.get(TRAVEL_COORDINATES);
                            ArrayList<LatLng> toPutInRes = new ArrayList<>();
                            assert tmp != null;
                            for (Object ob:tmp){
                                HashMap<String, Double> hash = (HashMap<String, Double>) ob;
                                LatLng each = new LatLng(
                                        Objects.requireNonNull(hash.get("latitude")),
                                        Objects.requireNonNull(hash.get("longitude"))
                                );
                                toPutInRes.add(each);
                            }
                            res.setTravelCoordinates(toPutInRes);

                            fetchedData.add(res);
                        }
                        listener.onDataFetched(null);
                        liveResultData.postValue(fetchedData);
                    }
                    else{
                        listener.onDataFetched(context.getString(R.string.error_fetch));
                        Log.i(TAG, "storeData: failure "+ Objects.requireNonNull(task.getException()).getMessage());
                    }
                });
    }
    //https://console.firebase.google.com/v1/r/project/distance-tracker-app-63ebc/firestore/indexes?create_composite=CmFwcm9qZWN0cy9kaXN0YW5jZS10cmFja2VyLWFwcC02M2ViYy9kYXRhYmFzZXMvKGRlZmF1bHQpL2NvbGxlY3Rpb25Hcm91cHMvdHJhdmVsX2hpc3RvcnkvaW5kZXhlcy9fEAEaCAoEdXNlchABGg8KC3NlcnZlcl90aW1lEAEaDAoIX19uYW1lX18QAQ

    public static void deleteData(Context context, String id, OnFirebaseResultListener listener){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        mFirestore.collection(HISTORY_COLLECTION)
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> listener.onFirebaseResult("Deleted"))
                .addOnFailureListener(e -> {
                    listener.onFirebaseResult(context.getString(R.string.error_fetch));
                    Log.i(TAG, "deleteData: "+e.getMessage());
                });
    }

    public static boolean isVerifiedUser(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()) {
            return true;
        } else if(mAuth.getCurrentUser() !=null )
            mAuth.signOut();
        return false;
    }

}
