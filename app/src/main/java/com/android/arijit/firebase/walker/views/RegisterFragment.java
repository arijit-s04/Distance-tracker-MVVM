package com.android.arijit.firebase.walker.views;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.arijit.firebase.walker.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    EditText mFullname, mEmail, mPassword;
    Button mRegisterBtn;
    TextView mLoginBtn;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    FirebaseFirestore fstore;
    String userID;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_register, container, false);

        mFullname   =   root.findViewById(R.id.fullName);
        mEmail      =   root.findViewById(R.id.Email);
        mPassword   =   root.findViewById(R.id.Password);
        mRegisterBtn=   root.findViewById(R.id.registerBtn);
        mLoginBtn   =   root.findViewById((R.id.Register));

        fAuth       =   FirebaseAuth.getInstance();
        progressBar =   root.findViewById(R.id.progressBar);
        fstore      =   FirebaseFirestore.getInstance();

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String fullName = mFullname.getText().toString().trim();

                boolean flag = false;

                if (TextUtils.isEmpty(email)) {
                    mEmail.setError("Email is required");
                    flag = true;
                }

                if (password.length() < 6) {
                    mPassword.setError("Password must be atleast 6 characters");
                    flag = true;
                }

                if (TextUtils.isEmpty(fullName)) {
                    mFullname.setError("Name cannot be empty");
                    flag = true;
                }

                if (flag) return;

                progressBar.setVisibility(View.VISIBLE);

                //registering the user in Firebase
                fAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                UserProfileChangeRequest changeRequest = new UserProfileChangeRequest
                                        .Builder().setDisplayName(fullName).build();
                                FirebaseUser user = authResult.getUser();
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
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                user.sendEmailVerification()
                                                    .addOnCompleteListener(task -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        if(fAuth!= null)
                                                            fAuth.signOut();
                                                        if (task.isSuccessful()) {
                                                            Snackbar.make(getView(), "Account created. Please verify the email sent to you.", Snackbar.LENGTH_LONG)
                                                                    .show();
                                                            resetForm();
                                                        } else {
                                                            Snackbar.make(getView(), task.getException().getMessage(), Snackbar.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                    });

                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            progressBar.setVisibility(View.GONE);
                                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG)
                                                    .show();
                                        });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);
                                Snackbar.make(getView(), e.getMessage(),Snackbar.LENGTH_LONG)
                                        .show();

                            }
                        })
                ;
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                        .replace(R.id.fragmentContainerView, LoginFragment.newInstance(null, null))
                        .commit();
            }
        });

        return root;
    }

    private void resetForm(){
        mFullname.setText("");
        mEmail.setText("");
        mPassword.setText("");
    }

}