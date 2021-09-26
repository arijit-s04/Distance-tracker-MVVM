package com.android.arijit.firebase.walker.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.android.arijit.firebase.walker.interfaces.OnDataFetchedListener;
import com.android.arijit.firebase.walker.models.ResultData;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;

import java.util.ArrayList;
import java.util.Objects;

public class HistoryListViewModel extends ViewModel {
    private MutableLiveData<ArrayList<ResultData>> historyLiveList;

    public LiveData<ArrayList<ResultData>> getHistoryLiveList(OnDataFetchedListener listener) {
        if( historyLiveList == null ){
            historyLiveList = new MutableLiveData<>();
            FirebaseUtil.fetchData(this, listener);
        }
        if( listener != null )  listener.onDataFetched(null);
        return historyLiveList;
    }

    public void setHistoryLiveList(ArrayList<ResultData> arrayList){
        this.historyLiveList.setValue(arrayList);
    }

    public void addResultData(ResultData resultData) {
       Objects.requireNonNull(this.historyLiveList.getValue()).add(resultData);
       this.historyLiveList.setValue(this.historyLiveList.getValue());
    }

}
