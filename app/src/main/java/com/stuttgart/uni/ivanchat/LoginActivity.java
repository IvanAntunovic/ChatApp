package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginButton;
    private ProgressDialog mLoginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginProgress = new ProgressDialog(this);

        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLoginButton = (Button) findViewById(R.id.login_btn);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if ( !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) ) {

                    if (password.length() < MIN_PASSWORD_LENGTH) {

                        Toast.makeText(LoginActivity.this, "Sorry dude, but the password should atleast contain 6 characters. You gotta protect that selfies if you know what I mean. Please try again.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    mLoginProgress.setTitle("Logging in");
                    mLoginProgress.setMessage("Please wait while we are checking your credentials");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email, password);

                } else {

                    Toast.makeText(LoginActivity.this, "C'mon dude, none of the input fields should be empty.", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    private void loginUser(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if ( task.isSuccessful() ){

                            mLoginProgress.dismiss();
                            Toast.makeText(LoginActivity.this, "You have successfully logged in", Toast.LENGTH_SHORT).show();
                            startMainActivity();

                        } else {

                            String errorMessage = task.getException().getMessage();
                            mLoginProgress.hide();
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void startMainActivity() {

        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        super.startActivity(mainIntent);
        super.finish();

    }


}
