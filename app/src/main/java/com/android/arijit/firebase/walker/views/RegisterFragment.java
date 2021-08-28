package com.android.arijit.firebase.walker.views;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.arijit.firebase.walker.R;
import com.android.arijit.firebase.walker.databinding.FragmentRegisterBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {}*/
    }

    private FirebaseAuth fAuth;
    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        String nextScreenInfo = "Already registered? Login here";
        SpannableString ss = new SpannableString(nextScreenInfo);
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                getParentFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .replace(R.id.fragmentContainerView, new LoginFragment())
                    .commit();
            }
        }, 20, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.Register.setText(ss);
        binding.Register.setMovementMethod(LinkMovementMethod.getInstance());
        binding.Register.setHighlightColor(Color.TRANSPARENT);

        binding.registerBtn.setOnClickListener(v -> {
            String email = binding.Email.getText().toString().trim();
            String password = binding.Password.getText().toString().trim();
            String fullName = binding.fullName.getText().toString().trim();

            boolean flag = false;

            if (TextUtils.isEmpty(email)) {
                binding.Email.setError("Email is required");
                flag = true;
            }

            if (password.length() < 6) {
                binding.Password.setError("Password must be atleast 6 characters");
                flag = true;
            }

            if (TextUtils.isEmpty(fullName)) {
                binding.fullName.setError("Name cannot be empty");
                flag = true;
            }

            if (flag) return;

            binding.progressBar.setVisibility(View.VISIBLE);

            //registering the user in Firebase
            fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        UserProfileChangeRequest changeRequest = new UserProfileChangeRequest
                                .Builder().setDisplayName(fullName).build();
                        FirebaseUser user = authResult.getUser();
                        Objects.requireNonNull(user);
                        user.updateProfile(changeRequest);
                        String createdId = user.getUid();
                        HashMap<String, Object> userObj = new HashMap<>();
                        userObj.put("userid", createdId);
                        userObj.put("username", fullName);
                        userObj.put("createdate", FieldValue.serverTimestamp());

                        FirebaseFirestore.getInstance()
                                .collection("usercollection")
                                .document(createdId)
                                .set(userObj)
                                .addOnSuccessListener(unused ->
                                    user.sendEmailVerification()
                                        .addOnCompleteListener(task -> {
                                            binding.progressBar.setVisibility(View.GONE);
                                            if(fAuth!= null)
                                                fAuth.signOut();
                                            if (task.isSuccessful()) {
                                                Snackbar.make(binding.getRoot(), "Account created. Please verify the email sent to you.", Snackbar.LENGTH_LONG)
                                                        .show();
                                                resetForm();
                                            } else {
                                                Snackbar.make(binding.getRoot(), Objects.requireNonNull(Objects.requireNonNull(task.getException()).getMessage()), Snackbar.LENGTH_LONG)
                                                        .show();
                                            }
                                        }))
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    Snackbar.make(binding.getRoot(), Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG)
                                            .show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Snackbar.make(binding.getRoot(), Objects.requireNonNull(e.getMessage()),Snackbar.LENGTH_LONG)
                                .show();

                    })
            ;
        });
    }

    private void resetForm(){
        binding.fullName.setText("");
        binding.Email.setText("");
        binding.Password.setText("");
    }

}