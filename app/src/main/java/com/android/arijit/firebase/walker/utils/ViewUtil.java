package com.android.arijit.firebase.walker.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.arijit.firebase.walker.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;

public class ViewUtil {
    public static void setMapTheme(@NonNull Context context, GoogleMap mMap){
        int res,
            nightModeFlag = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlag == Configuration.UI_MODE_NIGHT_YES) {
            res = R.raw.style_json_night;
        } else {
            res = R.raw.style_json;
        }
        try {
            boolean success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, res));
            if (!success) {
                Log.i("TAG", "onMapReady: parse failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.i("TAG", "onMapReady: style not found");
        }
    }
}
