package com.android.arijit.firebase.walker.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.android.arijit.firebase.walker.MainActivity;
import com.android.arijit.firebase.walker.activities.AccountActivity;
import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.FragmentSettiingsBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("DefaultLocale")
    public static String distanceFormat(float d){
        String ret = "Distance travelled : ";
        if (SYSTEM_UNIT == 0) {
            if (d > 1000f) {
                d = d / 1000f;
                return ret + String.format("%.2f km", d);
            } else {
                return ret + String.format("%.2f m", d);
            }
        }
        else{
            d = d/1609.34f;
            return ret + String.format("%.2f mi", d);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {}

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((BottomNavigationView)requireActivity().findViewById(R.id.navigation))
                        .setSelectedItemId(R.id.navigation_home);
            }
        });
    }

    public static int SYSTEM_THEME = 0;
    public static int SYSTEM_UNIT = 0;
    public static String SH = "Settings Preference";
    /*
     * tags for settings
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    private static final String TAG = "SettingsFragment";
    public static float CAMERA_ZOOM=16;
    public static float CAMERA_TILT=0;
    public static float CAMERA_BEARING=0;
    public static final float DEFAULT_ZOOM = 16f;
    public static final int DEFAULT_COUNT_DOWN = 3;
    public final static String LOGOUT_KEY = "logout";

    private final String[] themes = {"System", "Light", "Dark"};
    private final String[] unit = {"km/m", "miles"};
    private FragmentSettiingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettiingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        binding.tvName.setText(Objects.requireNonNull(mAuth.getCurrentUser()).getDisplayName());
        binding.tvEmail.setText(mAuth.getCurrentUser().getEmail());
        binding.logout.setOnClickListener(v->
            new AlertDialog.Builder(requireContext())
                .setTitle(requireContext().getString(R.string.logout))
                .setMessage(requireContext().getString(R.string.logout_warning))
                .setPositiveButton(requireContext().getString(R.string.yes), (dialog, which) -> {
                    resetSettings();
                    mAuth.signOut();
                    requireActivity().startActivity(new Intent(requireContext(), AccountActivity.class).putExtra(LOGOUT_KEY, true));
                    requireActivity().finish();
                })
                .setNegativeButton(requireContext().getString(R.string.no), null)
                .create().show()
        );

        currSettingsHint();

    }

    private void currSettingsHint(){
       binding.autoCompleteTextView2.setText(themes[SYSTEM_THEME]);
       binding.autoCompleteTextView4.setText(unit[SYSTEM_UNIT]);
    }

    @Override
    public void onResume() {
        ArrayAdapter<String>myAdapter = new ArrayAdapter<>(requireContext(),
                R.layout.themes_dropdown, themes);
        myAdapter.setDropDownViewResource(R.layout.themes_dropdown);
        binding.autoCompleteTextView2.setAdapter(myAdapter);
        binding.autoCompleteTextView2.setOnItemClickListener((parent, view, position, id) -> setTheme(position));

        ArrayAdapter<String>myAdapter2 = new ArrayAdapter<>(requireContext(),
                R.layout.themes_dropdown, unit);
        myAdapter.setDropDownViewResource(R.layout.themes_dropdown);
        binding.autoCompleteTextView4.setAdapter(myAdapter2);
        binding.autoCompleteTextView4.setOnItemClickListener((parent, view, position, id) -> setUnit(position));
        super.onResume();
    }

    private void setTheme(int themeID){

        if(SYSTEM_THEME == themeID){
            return;
        }

        switch (themeID){
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                Log.i(TAG, "setTheme: here");
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default: break;
        }

        SYSTEM_THEME = themeID;
        SharedPreferences.Editor shEditor = requireActivity()
                .getSharedPreferences(SH, Context.MODE_PRIVATE)
                .edit();
        shEditor.putInt(MainActivity.THEME_KEY, themeID);
        shEditor.apply();
    }

    private void setUnit(int unitID){
        SYSTEM_UNIT = unitID;
        SharedPreferences.Editor shEditor = requireActivity()
                .getSharedPreferences(SH, Context.MODE_PRIVATE)
                .edit();
        shEditor.putInt(MainActivity.UNIT_KEY, unitID);
        shEditor.apply();
    }
    public void resetSettings(){
        SettingsFragment.SYSTEM_THEME = 0;
        SettingsFragment.SYSTEM_UNIT = 0;
        requireActivity().getSharedPreferences(SettingsFragment.SH, Context.MODE_PRIVATE).edit()
                .putInt(MainActivity.THEME_KEY, SettingsFragment.SYSTEM_THEME)
                .putInt(MainActivity.UNIT_KEY, SettingsFragment.SYSTEM_UNIT)
                .apply();
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

