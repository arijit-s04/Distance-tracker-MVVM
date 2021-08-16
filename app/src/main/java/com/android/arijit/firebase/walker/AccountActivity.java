package com.android.arijit.firebase.walker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
//            startActivity(new Intent(this, MainActivity.class));
//            this.finish();
//        }
        if(getIntent().getBooleanExtra("logout", false)){
            getIntent().removeExtra("logout");
            Snackbar.make(this.findViewById(R.id.fragmentContainerView), "Logout Successful", Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
}