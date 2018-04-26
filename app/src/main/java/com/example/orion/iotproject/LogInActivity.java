package com.example.orion.iotproject;


import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class LogInActivity extends AppCompatActivity {

    private static final String TAG = "FBLOG";
    private FirebaseAuth mAuth;
    private Button  signinButton;
    private EditText usernameEditText, passwordEditText;
    public ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        usernameEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        signinButton = findViewById(R.id.button3);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Facebook Login button



        //Sign in with email
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                String email = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!email.isEmpty() && !password.isEmpty()) {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        hideProgressDialog();
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        Toast.makeText(LogInActivity.this, "Failed to log in", Toast.LENGTH_SHORT).show();
                                    } else {
                                        hideProgressDialog();
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        Toast.makeText(LogInActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                                        updateUI();

                                    }
                                }
                            });
                } else {
                    Toast.makeText(LogInActivity.this, "One or more fields are empty", Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            updateUI();
        }

    }

    private void updateUI() {
        Toast.makeText(LogInActivity.this, "Your are Logged in", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(LogInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please Wait");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}
