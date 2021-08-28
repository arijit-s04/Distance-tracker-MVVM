package com.android.arijit.firebase.walker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.views.SettingsFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        if(getIntent().getBooleanExtra(SettingsFragment.LOGOUT_KEY, false)){
            getIntent().removeExtra(SettingsFragment.LOGOUT_KEY);
            Snackbar.make(this.findViewById(R.id.fragmentContainerView), getString(R.string.logout_success), Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}