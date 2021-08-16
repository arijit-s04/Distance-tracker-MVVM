package com.android.arijit.firebase.walker;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
    EditText mEmail, mPassword;
    Button mLoginBtn;
    TextView mCreateBtn;
    ProgressBar mprogressBar;
    FirebaseAuth fAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_login, container, false);

        mEmail      =   root.findViewById(R.id.Email);
        mPassword   =   root.findViewById((R.id.Password));
        fAuth       =   FirebaseAuth.getInstance();
        mLoginBtn   =   root.findViewById((R.id.login));
        mCreateBtn  =   root.findViewById(R.id.Register);
        mprogressBar =   root.findViewById(R.id.progressBar2);


        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email    =   mEmail.getText().toString().trim();
                String password =   mPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
                {
                    mEmail.setError("Email is required");
                    mPassword.setError("Password is required");
                    return;
                }
                if(TextUtils.isEmpty(email))
                {
                    mEmail.setError("Email is required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    mPassword.setError("Password is required");
                    return;
                }

                mprogressBar.setVisibility(View.VISIBLE);

                //check the authenticity of the user

                fAuth.signInWithEmailAndPassword(email,password)
                    .addOnFailureListener(e -> {
                        mprogressBar.setVisibility(View.GONE);
                        Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                    })
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        mprogressBar.setVisibility(View.GONE);
                        if(!user.isEmailVerified()){

                            Snackbar.make(v, "Please Verify Your Email", Snackbar.LENGTH_LONG)
                                    .setAction("Verify", inner -> {
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(task -> {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(), "Email Sent", Toast.LENGTH_SHORT).show();
                                                    } else{
                                                        Log.i("logerror", "onClick: "+task.getException().getMessage());
                                                        Toast.makeText(getContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    })
                                    .show();
                            fAuth.signOut();
                        } else {
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            intent.putExtra("virgin", true);
                            getActivity().startActivity(intent);
                            getActivity().finish();
                        }            //
                    });

            }
        });


        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.fragmentContainerView, RegisterFragment.newInstance(null, null))
                        .commit();
            }
        });

        return root;
    }
}