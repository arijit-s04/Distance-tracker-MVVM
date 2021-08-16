package com.android.arijit.firebase.walker;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class FirebaseHelper {
    private static String TAG = "FirebaseHelper";
    public static MutableLiveData<ArrayList<ResultData>> liveResultData = new MutableLiveData<>();
    static String DATE = "date",
            TIME = "time",
            DISTANCE = "distance",
            TRAVEL_COORDINATES = "travel_coordinates",
            SERVER_TIME = "server_time",
            HISTORY_COLLECTION = "travel_history",
            USER = "user";

    public static void storeData(ResultData data, View v){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String email ;
        try{
            email = mAuth.getCurrentUser().getEmail();
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
                    Snackbar.make(v, "Unable to save data", Snackbar.LENGTH_LONG).show();
                    Log.i(TAG, "storeData: failure "+ error.getMessage());
                });
    }

    public static void fetchData(View v){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String email ;
        try{
            email = mAuth.getCurrentUser().getEmail();
        } catch (Exception e){
            email = "email";
        }
        ArrayList<ResultData> fetchedData = new ArrayList<>();
        liveResultData.setValue(fetchedData);

        mFirestore.collection(HISTORY_COLLECTION)
                .orderBy(SERVER_TIME)
                .whereEqualTo(USER, email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isComplete()){
                            Log.i(TAG, "onComplete: "+task.getResult().size());
                            for (QueryDocumentSnapshot docSnap : task.getResult()){
                                ResultData res = new ResultData();
                                res.setId(docSnap.getId());
                                res.setDate(docSnap.getString(DATE));
                                res.setTime(docSnap.getString(TIME));
                                res.setDistanceTravelled(Float.valueOf(docSnap.get(DISTANCE).toString()));
                                ArrayList<Object> tmp = (ArrayList<Object>) docSnap.get(TRAVEL_COORDINATES);
                                ArrayList<LatLng> toPutInRes = new ArrayList<>();
                                for (Object ob:tmp){
                                    HashMap<String, Double> hash = (HashMap<String, Double>) ob;
                                    LatLng each = new LatLng(
                                            hash.get("latitude"), hash.get("longitude")
                                    );
                                    toPutInRes.add(each);
                                }
                                res.setTravelCoordinates(toPutInRes);

                                fetchedData.add(res);
                            }
                            v.setVisibility(View.GONE);
                            liveResultData.postValue(fetchedData);
                        }
                        else{
                            try {
                                Snackbar.make(v, "Something went wrong", Snackbar.LENGTH_LONG).show();
                            } catch (Exception e){
                                Log.i(TAG, "onComplete: "+e.getLocalizedMessage());
                            }
                            Log.i(TAG, "storeData: failure "+ task.getException().getMessage());
                        }
                    }
                });
    }
    //https://console.firebase.google.com/v1/r/project/distance-tracker-app-63ebc/firestore/indexes?create_composite=CmFwcm9qZWN0cy9kaXN0YW5jZS10cmFja2VyLWFwcC02M2ViYy9kYXRhYmFzZXMvKGRlZmF1bHQpL2NvbGxlY3Rpb25Hcm91cHMvdHJhdmVsX2hpc3RvcnkvaW5kZXhlcy9fEAEaCAoEdXNlchABGg8KC3NlcnZlcl90aW1lEAEaDAoIX19uYW1lX18QAQ

    public static void deleteData(String id, View v){
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        String email ;
        try{
            email = mAuth.getCurrentUser().getEmail();
        } catch (Exception e){
            email = "email";
        }

        mFirestore.collection(HISTORY_COLLECTION)
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Snackbar.make(v, "Deleted", Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(v ,"Something went wrong", Snackbar.LENGTH_LONG).show();
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
