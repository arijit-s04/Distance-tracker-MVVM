package com.android.arijit.firebase.walker.viewmodel;

import android.content.Context;

import androidx.databinding.ObservableField;
import androidx.databinding.ObservableFloat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.models.ResultData;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Objects;

public class LocationViewModel extends ViewModel {

    private ResultData resultData;
    public final ObservableFloat distInMetre = new ObservableFloat();
    private final MutableLiveData<ArrayList<LatLng>> curGotPosition = new MutableLiveData<>();
    private final MutableLiveData<Boolean> trackState = new MutableLiveData<>(Boolean.FALSE);
    public final ObservableField<String> resDate = new ObservableField<>();
    public final ObservableField<String> resTime = new ObservableField<>();

    public LocationViewModel() {
        this.distInMetre.set(0f);
        this.curGotPosition.setValue(new ArrayList<>());
        this.resultData = new ResultData();
        setDateTime();
    }

    public ObservableFloat getDistInMetre() {
        return distInMetre;
    }

    public LiveData<ArrayList<LatLng>> getCurGotPosition() {
        return curGotPosition;
    }

    public LatLng getLastCoordinates() {
        int size = Objects.requireNonNull(curGotPosition.getValue()).size();
        return curGotPosition.getValue().get(size - 1);
    }

    public boolean isCoorListEmpty() {
        return Objects.requireNonNull(curGotPosition.getValue()).isEmpty();
    }

    public void addLatLng(LatLng latLng) {
        Objects.requireNonNull(this.curGotPosition.getValue()).add(latLng);
        this.curGotPosition.setValue(this.curGotPosition.getValue());
    }

    public void setDistInMetre(float distInMetre) {
        this.distInMetre.set(distInMetre);
    }

    public void setCurGotPosition(ArrayList<LatLng> curGotPosition) {
        this.curGotPosition.setValue(curGotPosition);
    }

    public boolean getTrackState() {
        return Objects.requireNonNull(this.trackState.getValue());
    }

    public void setTrackState(boolean state){
        this.trackState.setValue(state);
    }

    public void setResultData(ResultData resultData) {
        this.resultData = resultData;
        setDateTime();
    }

    public ResultData getResultData() {
        return resultData;
    }

    public void saveResult(Context context, OnFirebaseResultListener listener) {
        resultData.setDistanceTravelled(this.distInMetre.get());
        resultData.setTravelCoordinates(this.curGotPosition.getValue());
        setDateTime();
        FirebaseUtil.storeData(context, this.getResultData(), listener);
    }

    private void setDateTime() {
        this.resDate.set(resultData.getDate());
        this.resTime.set(resultData.getTime());
    }

}
