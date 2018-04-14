package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DisplayNameActivity extends AppCompatActivity {

    private static final String USER_NAME_KEY_VALUE = "user_name_value";
    private static final String ACTION_BAR_TITLE = "Account Status";
    private static final String DATABASE_ROOT = "Users";

    private Toolbar mToolbar;
    private TextInputLayout mUserName;
    private Button mSaveUserNameButton;

    // Firebase
    private DatabaseReference mDisplayNameDatabase;
    private FirebaseUser mCurrentUser;

    // Progressdialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_name);

        mToolbar = (Toolbar) findViewById(R.id.display_name_appBar);
        mUserName = (TextInputLayout) findViewById(R.id.display_name_input);
        mSaveUserNameButton = (Button) findViewById(R.id.display_name_save_btn);

        // Set toolbar and back button visible
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(ACTION_BAR_TITLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDisplayNameDatabase = FirebaseDatabase.getInstance().getReference().child(DATABASE_ROOT).child(currentUserUid);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Retrieve & update with previous user display name from the Settings activity
        String currentUserDisplayName = getIntent().getStringExtra(USER_NAME_KEY_VALUE);
        mUserName.getEditText().setText(currentUserDisplayName);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

        mSaveUserNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Progress
                mProgressDialog = new ProgressDialog(DisplayNameActivity.this);
                mProgressDialog.setTitle("Saving changes");
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.show();

                String displayName = mUserName.getEditText().getText().toString();

                mDisplayNameDatabase.child("name").setValue(displayName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Dude you have just got new name.", Toast.LENGTH_LONG).show();

                            startSettingsActivity();

                        } else {

                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }
        });

    }

    private void startSettingsActivity() {

        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        super.startActivity(settingsIntent);
        super.finish();

    }
}
