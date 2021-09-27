package com.android.arijit.firebase.walker.utils;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.applications.App;
import com.android.arijit.firebase.walker.views.HomeFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ViewUtil {
    private final static int ANIM_TIME = 500;

    public static void setMapTheme(@NonNull Context context, GoogleMap mMap){
        int res,
            nightModeFlag = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlag == Configuration.UI_MODE_NIGHT_YES) {
            res = R.raw.style_json_night;
            HomeFragment.POLYLINE_COLOR = Color.WHITE;
        } else {
            res = R.raw.style_json;
            HomeFragment.POLYLINE_COLOR = Color.BLACK;
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

    public static ValueAnimator animatorForFab(FloatingActionButton fab){
        int start = App.getContext().getColor(R.color.fab_blue);
        int end = App.getContext().getColor(R.color.stop_red);
        final Bitmap direction = getBitmapFromVectorDrawable(App.getContext(), R.drawable.ic_baseline_directions_24);
        final Bitmap clear = getBitmapFromVectorDrawable(App.getContext(), R.drawable.ic_baseline_clear_24);
        float[] steps = new float[3];
        steps[0] = (Color.red(end) - Color.red(start)) / (ANIM_TIME * 0.5f);
        steps[1] = (Color.green(end) - Color.green(start)) / (ANIM_TIME * 0.5f);
        steps[2] = (Color.blue(end) - Color.blue(start)) / (ANIM_TIME * 0.5f);
        @SuppressLint("Recycle") ValueAnimator valueAnimator = ValueAnimator.ofInt(0, ANIM_TIME/2 - 1).setDuration(ANIM_TIME);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            int r, g, b;
            r = (int) (Color.red(start) + steps[0]*value);
            g = (int) (Color.green(start) + steps[1]*value);
            b = (int) (Color.blue(start) + steps[2]*value);
            fab.setBackgroundTintList(
                    ColorStateList.valueOf(Color.rgb(r, g, b))
            );
            fab.setImageBitmap(createFabBitmap(direction, clear, value));
        });
        return valueAnimator;
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        assert (drawable != null);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap createFabBitmap(Bitmap direction, Bitmap clear, int value){
        int width = direction.getWidth(), height = direction.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(result);
        Paint paint = new Paint();
        /**/
        canvas.save();
        canvas.rotate(-1*calculateAngle(value), width/2.0f, height/2.0f);
        paint.setAlpha(255 - calculateAlpha(value));
        canvas.drawBitmap(direction, 0, 0, paint);
        canvas.restore();
        /**/
        canvas.save();
        canvas.rotate(-1*calculateAngle(value), width/2.0f, height/2.0f);
        paint.setAlpha(calculateAlpha(value));
        canvas.drawBitmap(clear, 0, 0, paint);
        canvas.restore();
        /**/
        return result;
    }

    private static float calculateAngle(int value){
        return (180.0f*value*2)/ANIM_TIME;
    }

    private static int calculateAlpha(int value){
        return (int) ((value*255.0f*2)/ANIM_TIME);
    }

}
