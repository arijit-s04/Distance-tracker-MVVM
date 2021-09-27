package com.android.arijit.firebase.walker.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.arijit.firebase.walker.MainActivity;
import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.FragmentLoginBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {}*/
    }

    private FirebaseAuth fAuth;
    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        setOnClickListeners();
        String nextScreenInfo = "Don't have an account? Create account";
        SpannableString ss = new SpannableString(nextScreenInfo);
//        ClickableSpan clickableSpan = ;
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.fragmentContainerView, new RegisterFragment())
                    .commit();
            }
        }, 23, 37, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.Register.setText(ss);
        binding.Register.setMovementMethod(LinkMovementMethod.getInstance());
        binding.Register.setHighlightColor(Color.TRANSPARENT);
    }

    private void setOnClickListeners() {
        binding.login.setOnClickListener(v -> {

            String email    =   binding.Email.getText().toString().trim();
            String password =   binding.Password.getText().toString().trim();

            if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
            {
                binding.Email.setError("Email is required");
                binding.Password.setError("Password is required");
                return;
            }
            if(TextUtils.isEmpty(email))
            {
                binding.Email.setError("Email is required");
                return;
            }
            if(TextUtils.isEmpty(password)){
                binding.Password.setError("Password is required");
                return;
            }

            binding.progressBar2.setVisibility(View.VISIBLE);

            //check the authenticity of the user

            fAuth.signInWithEmailAndPassword(email,password)
                    .addOnFailureListener(e -> {
                        binding.progressBar2.setVisibility(View.GONE);
                        Snackbar.make(v, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT)
                                .show();
                    })
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        Objects.requireNonNull(user);
                        binding.progressBar2.setVisibility(View.GONE);
                        if(!user.isEmailVerified()){

                            Snackbar.make(v, "Please Verify Your Email", Snackbar.LENGTH_LONG)
                                    .setAction("Verify", inner -> user.sendEmailVerification()
                                            .addOnCompleteListener(task -> {
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Objects.requireNonNull(task.getException());
                                                    Log.e("logError", "onClick: "+task.getException().getMessage());
                                                    Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }))
                                    .show();
                            fAuth.signOut();
                        } else {
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("virgin", true);
                            requireActivity().startActivity(intent);
                            requireActivity().finish();
                        }
                    });

        });

    }
}