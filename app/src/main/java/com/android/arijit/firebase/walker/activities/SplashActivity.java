package com.android.arijit.firebase.walker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.android.arijit.firebase.walker.MainActivity;
import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.ActivitySplashBinding;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        Animation topDown = AnimationUtils.loadAnimation(this, R.anim.top_down);
        Animation botUp = AnimationUtils.loadAnimation(this, R.anim.bottom_up);
        Animation botUpTag = AnimationUtils.loadAnimation(this, R.anim.bottom_up_tag);

        binding.splashLogo.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        binding.splashLogo.startAnimation(topDown);
        binding.splashName.startAnimation(botUp);
        binding.splashTag.startAnimation(botUpTag);

        new Handler().postDelayed(() -> {
            if(FirebaseUtil.isVerifiedUser()) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            else{
                startActivity(new Intent(SplashActivity.this, AccountActivity.class));
            }
            finish();
        }, 2500);

    }

}