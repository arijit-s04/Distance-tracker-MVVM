package com.android.arijit.firebase.walker.models;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ResultData {
    private String id;
    private String date, time;
    private float distanceTravelled;
    private ArrayList<LatLng> travelCoordinates;
    public ResultData(){
        java.util.Date d = new java.util.Date();
        this.date = new SimpleDateFormat("MMM d yyyy", Locale.getDefault()).format(d);
        this.time = new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(d);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getDistanceTravelled() {
        return distanceTravelled;
    }

    public void setDistanceTravelled(float distanceTravelled) {
        this.distanceTravelled = distanceTravelled;
    }

    public ArrayList<LatLng> getTravelCoordinates() {
        return travelCoordinates;
    }

    public void setTravelCoordinates(ArrayList<LatLng> travelCoordinates) {
        this.travelCoordinates = travelCoordinates;
    }
}
