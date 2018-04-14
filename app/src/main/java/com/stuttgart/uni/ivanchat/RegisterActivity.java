package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

import user.User;

public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    //ProgressDialog
    private ProgressDialog mRegProgress;

    private ProgressDialog getmRegProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Android Fields
        mDisplayName = (TextInputLayout) findViewById(R.id.register_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.register_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String displayName = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(displayName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    showRegistrationProgressDialog();

                    registerUser(displayName, email, password);

                } else {

                    Toast.makeText(RegisterActivity.this, "C'mon dude, none of the input fields should be empty.", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void showRegistrationProgressDialog() {

        mRegProgress.setTitle("Registering User");
        mRegProgress.setMessage("Please wait while we create your account");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();

    }

    private void registerUser(final String displayName, final String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    String currentUserUid = null;
                    if ( currentUser != null ) {

                        currentUserUid = currentUser.getUid();

                    }

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserUid);

                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put(UserData.NAME_DEFAULT_VALUE, displayName);
                    userMap.put(UserData.STATUS_DEFAULT_VALUE, "Hi there I'm using Ivan Chat App.");
                    userMap.put(UserData.IMAGE_DEFAULT_VALUE, "default");
                    userMap.put(UserData.THUMB_IMAGE_DEFAULT_VALUE, "default");
                    userMap.put("device_token", device_token);

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mRegProgress.dismiss();
                                Toast.makeText(RegisterActivity.this, "Registration has been successful.", Toast.LENGTH_SHORT).show();

                                startMainActivity();
                            }
                        }
                    });

                } else {

                    mRegProgress.hide();
                    FirebaseAuthException e = (FirebaseAuthException) task.getException();

                    String error ="";

                    try {

                        throw task.getException();

                    } catch(FirebaseAuthWeakPasswordException ex) {
                        error = "Weak password! Use atleast 6 characters.";
                    } catch(FirebaseAuthInvalidCredentialsException ex){
                        error = "Invalid Email!";
                    } catch(FirebaseAuthUserCollisionException ex) {
                        error = "Existing Account!";
                    } catch(Exception ex) {
                        error = "Unknown error!";
                        ex.printStackTrace();
                    }

                    Toast.makeText(RegisterActivity.this, "Sorry dude. " + error, Toast.LENGTH_LONG).show();

                    return;
                }
            }
        });

    }

    private void startMainActivity() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
