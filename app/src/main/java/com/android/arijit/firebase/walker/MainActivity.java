package com.android.arijit.firebase.walker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {
    public static BottomNavigationView bottomNavigationView;
    private String TAG = "MainActivity";
    boolean isVirgin = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!FirebaseHelper.isVerifiedUser())
            finish();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isVirgin = getIntent().getBooleanExtra("virgin", false);

        if(isVirgin){
            getIntent().removeExtra("virgin");
            Snackbar.make(this.findViewById(R.id.navigation), "Login Successful", Snackbar.LENGTH_SHORT).show();
        }
        else {
            initSettings();
        }

        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);
        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.main_fragment_container, HomeFragment.newInstance(null, null))
                    .commit();
        }

    }

    private boolean loadFragment(Fragment fragment){
        if(fragment == null)    return false;

        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack("stack")
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_fragment_container,fragment)
                .commit();

        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
        Fragment fragment;
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()){
            case R.id.navigation_home:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_baseline_home_24);
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_outline_settings_24);
                fm.popBackStack();
                return true;
            case R.id.navigation_history:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_outline_home_24);
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_outline_settings_24);
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = HistoryFragment.newInstance(null, null);
                break;
            case R.id.navigation_settings:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_outline_home_24);
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_baseline_settings_24);
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = SettingsFragment.newInstance(null, null);
                break;
            default:
                fragment = null;
        }
        return loadFragment(fragment);
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {}

    @Override
    protected void onResume() {
        switch (bottomNavigationView.getSelectedItemId() ) {
            case R.id.navigation_settings:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_baseline_settings_24);
                break;
            case R.id.navigation_home:
                bottomNavigationView.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_baseline_home_24);
                break;
            default:
                break;
        }
        super.onResume();
    }

    public void initSettings(){
        SharedPreferences sh = getSharedPreferences(SettingsFragment.SH, Context.MODE_PRIVATE);
        SettingsFragment.SYSTEM_THEME = sh.getInt("theme", 0);
        SettingsFragment.SYSTEM_UNIT = sh.getInt("unit", 0);

        int nightMode;
        switch (SettingsFragment.SYSTEM_THEME){
            case 1:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case 2:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);

    }

}