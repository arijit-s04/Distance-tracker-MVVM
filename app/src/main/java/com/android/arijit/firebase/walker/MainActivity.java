package com.android.arijit.firebase.walker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.arijit.firebase.walker.databinding.ActivityMainBinding;
import com.android.arijit.firebase.walker.interfaces.OnFirebaseResultListener;
import com.android.arijit.firebase.walker.utils.FirebaseUtil;
import com.android.arijit.firebase.walker.views.HistoryFragment;
import com.android.arijit.firebase.walker.views.HomeFragment;
import com.android.arijit.firebase.walker.views.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener,
        OnFirebaseResultListener {
    private final String TAG = "MainActivity";
    boolean isVirgin = false;
    private ActivityMainBinding binding;
    public final static String THEME_KEY = "theme";
    public final static String UNIT_KEY = "unit";
    public final static String VIRGIN = "virgin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!FirebaseUtil.isVerifiedUser())
            finish();
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        isVirgin = getIntent().getBooleanExtra(VIRGIN, false);

        if(isVirgin){
            getIntent().removeExtra(VIRGIN);
            Snackbar.make(binding.navigation, "Login Successful", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.navigation)
                    .show();
        }
        else {
            initSettings();
        }
        binding.navigation.setOnNavigationItemSelectedListener(this);
        binding.navigation.setOnNavigationItemReselectedListener(this);
        if(savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.main_fragment_container, new HomeFragment(this))
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {
        Fragment fragment;
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()){
            case R.id.navigation_home:
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_baseline_home_24);
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_outline_settings_24);
                fm.popBackStack();
                return true;
            case R.id.navigation_history:
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_outline_home_24);
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_outline_settings_24);
                if(fm.getBackStackEntryCount() > 0){
                    fm.popBackStack();
                }
                fragment = new HistoryFragment(this);
                break;
            case R.id.navigation_settings:
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_outline_home_24);
                binding.navigation.getMenu()
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

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onResume() {
        switch (binding.navigation.getSelectedItemId() ) {
            case R.id.navigation_settings:
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_settings)
                        .setIcon(R.drawable.ic_baseline_settings_24);
                break;
            case R.id.navigation_home:
                binding.navigation.getMenu()
                        .findItem(R.id.navigation_home)
                        .setIcon(R.drawable.ic_baseline_home_24);
                break;
            default:
                break;
        }
        super.onResume();
    }

    @Override
    public void onFirebaseResult(@Nullable String result) {
        if(result == null) return;
        Snackbar.make(binding.getRoot(), result, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.navigation)
                .show();
    }
    public void initSettings(){
        SharedPreferences sh = getSharedPreferences(SettingsFragment.SH, Context.MODE_PRIVATE);
        SettingsFragment.SYSTEM_THEME = sh.getInt(THEME_KEY, 0);
        SettingsFragment.SYSTEM_UNIT = sh.getInt(UNIT_KEY, 0);

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