package com.stuttgart.uni.ivanchat;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ProfilePictureActivity extends AppCompatActivity {

    private static final String TOOLBAR_TITLE = "Profile Picture";

    private ImageView mDisplayImage;
    private Toolbar mToolbar;
    private DatabaseReference mFriendDatabase;

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

        mFriendDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());;

        mFriendDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    // Update picture in the profile view
                    String displayName = dataSnapshot.child(UserData.NAME_DEFAULT_VALUE).getValue().toString();
                    String status = dataSnapshot.child(UserData.STATUS_DEFAULT_VALUE).getValue().toString();
                    String image = dataSnapshot.child(UserData.IMAGE_DEFAULT_VALUE).getValue().toString();

                    Picasso.with(ProfilePictureActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                    PhotoViewAttacher photoView = new PhotoViewAttacher(mDisplayImage);
                    photoView.update();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }
}
