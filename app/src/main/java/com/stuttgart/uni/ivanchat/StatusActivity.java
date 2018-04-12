package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private static final String SETTINGS_ACTIVITY_STATUS_KEY_VALUE = "status_value";
    private static final String ACTION_BAR_TITLE = "Account Status";
    private static final String DATABASE_ROOT = "Users";

    private static final String LOG_TAG = "IvanMessage";

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button mSaveStatusButton;

    // Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    // Progressdialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Firebase
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child(DATABASE_ROOT).child(currentUserUid);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(ACTION_BAR_TITLE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveStatusButton = (Button) findViewById(R.id.status_save_btn);

        // Retrieve & update with previous user status from the Settings activity
        String currentUserStatusValue = getIntent().getStringExtra(SETTINGS_ACTIVITY_STATUS_KEY_VALUE);
        mStatus.getEditText().setText(currentUserStatusValue);

        mSaveStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Progress
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving changes");
                mProgressDialog.setMessage("Please wait");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()) {

                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Thanks dude for keeping me updated with your new status", Toast.LENGTH_LONG).show();

                        } else {

                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

                        }

                    }
                });
            }
        });

    }
}