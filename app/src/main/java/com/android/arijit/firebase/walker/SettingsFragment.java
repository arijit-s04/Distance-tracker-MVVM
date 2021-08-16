package com.android.arijit.firebase.walker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        getActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ((BottomNavigationView)getActivity().findViewById(R.id.navigation))
                        .setSelectedItemId(R.id.navigation_home);
            }
        });
    }

    public static int SYSTEM_THEME = 0;
    public static int SYSTEM_UNIT = 0;
    public static String SH = "Settings Preference";
    /**
     * tags for settings
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    private String TAG = "SettingsFragment";
    public static float CAMERA_ZOOM=16;
    public static float CAMERA_TILT=0;
    public static float CAMERA_BEARING=0;

    private AutoCompleteTextView myDropDwn;
    private AutoCompleteTextView myDropDwn2;
    private String[] themes = {"System", "Light", "Dark"};
    private String[] unit = {"km/m", "miles"};
    private TextView tvname, tvemail;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settiings, container, false);

        myDropDwn= (AutoCompleteTextView) root.findViewById(R.id.autoCompleteTextView2);
        myDropDwn2 = (AutoCompleteTextView) root.findViewById(R.id.autoCompleteTextView4);
        tvemail = root.findViewById(R.id.tv_email);
        tvname = root.findViewById(R.id.tv_name);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        tvname.setText(mAuth.getCurrentUser().getDisplayName());
        tvemail.setText(mAuth.getCurrentUser().getEmail());

        currSettingsHint();

        ((Button) root.findViewById(R.id.logout)).setOnClickListener(
                v -> {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Logout")
                            .setMessage("Do you want to logout?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetSettings();
                                    mAuth.signOut();
                                    getActivity().startActivity(new Intent(getContext(), AccountActivity.class).putExtra("logout", true));
                                    getActivity().finish();
                                }
                            })
                            .setNegativeButton("No", null)
                            .create().show();


                }
        );

        return root;
    }
    private void currSettingsHint(){
       myDropDwn.setText(themes[SYSTEM_THEME]);
       myDropDwn2.setText(unit[SYSTEM_UNIT]);
    }

    @Override
    public void onResume() {
        ArrayAdapter<String>myAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.themes_dropdown, themes);
        myAdapter.setDropDownViewResource(R.layout.themes_dropdown);
        myDropDwn.setAdapter(myAdapter);
        myDropDwn.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setTheme(position);
            }
        });

        ArrayAdapter<String>myAdapter2 = new ArrayAdapter<String>(getContext(),
                R.layout.themes_dropdown, unit);
        myAdapter.setDropDownViewResource(R.layout.themes_dropdown);
        myDropDwn2.setAdapter(myAdapter2);
        myDropDwn2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setUnit(position);
            }
        });
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
        SharedPreferences.Editor shEditor = getActivity()
                .getSharedPreferences(SH, Context.MODE_PRIVATE)
                .edit();
        shEditor.putInt("theme", themeID);
        shEditor.commit();
    }

    private void setUnit(int unitID){
        SYSTEM_UNIT = unitID;
        SharedPreferences.Editor shEditor = getActivity()
                .getSharedPreferences(SH, Context.MODE_PRIVATE)
                .edit();
        shEditor.putInt("unit", unitID);
        shEditor.commit();
    }
    public void resetSettings(){
        SettingsFragment.SYSTEM_THEME = 0;
        SettingsFragment.SYSTEM_UNIT = 0;
        getActivity().getSharedPreferences(SettingsFragment.SH, Context.MODE_PRIVATE).edit()
                .putInt("theme", SettingsFragment.SYSTEM_THEME)
                .putInt("unit", SettingsFragment.SYSTEM_UNIT)
                .commit();
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

