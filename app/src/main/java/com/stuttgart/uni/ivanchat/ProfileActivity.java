package com.stuttgart.uni.ivanchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        String userId = getIntent().getStringExtra(IntentData.USERS_TO_PROFILE_PICTURE_USER_ID);

        mDisplayId = (TextView) findViewById(R.id.profile_displayName);
        mDisplayId.setText(userId);

    }
}
