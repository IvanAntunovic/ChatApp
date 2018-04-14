package com.stuttgart.uni.ivanchat;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final String TOOLBAR_TITLE = "Profile Picture";

    private ImageView mDisplayImage;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_picture);

        mDisplayImage = (ImageView) findViewById(R.id.profile_picture_displayImage);
        mToolbar = (Toolbar) findViewById(R.id.profile_picture_appBar);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {

            getSupportActionBar().setTitle(TOOLBAR_TITLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        String profilePicture = getIntent().getStringExtra(IntentData.SETTINGS_TO_PROFILE_PICTURE_DISPLAY_IMAGE);
        Picasso.with(ProfilePictureActivity.this).load(profilePicture).placeholder(R.drawable.default_avatar).into(mDisplayImage);

        PhotoViewAttacher photoView = new PhotoViewAttacher(mDisplayImage);
        photoView.update();

    }
}
