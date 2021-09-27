package com.android.arijit.firebase.walker.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.ActivityAccountBinding;
import com.android.arijit.firebase.walker.views.SettingsFragment;
import com.google.android.material.snackbar.Snackbar;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAccountBinding binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(getIntent().getBooleanExtra(SettingsFragment.LOGOUT_KEY, false)){
            getIntent().removeExtra(SettingsFragment.LOGOUT_KEY);
            Snackbar.make(binding.fragmentContainerView, getString(R.string.logout_success), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}