package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "IvanMessage";

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount, mProfileFriendsText;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private ProgressDialog mProgressDialog;
    private FirebaseUser mCurrentUser;

    private UserRequestManager mUserRequestManager;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra(IntentData.USERS_TO_PROFILE_PICTURE_USER_ID);

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileFriendsText = (TextView) findViewById(R.id.profile_friends_textView);
        mProfileFriendsText.setVisibility(View.INVISIBLE);

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_btn);

        mUserRequestManager = new UserRequestManager(
                mUsersDatabase,
                mFriendRequestDatabase,
                mFriendDatabase,
                mNotificationDatabase,
                mProfileSendReqBtn,
                mDeclineBtn,
                mProfileFriendsText
        );

        this.startProgressDialog();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child(UserData.NAME_DEFAULT_VALUE).getValue().toString();
                String status = dataSnapshot.child(UserData.STATUS_DEFAULT_VALUE).getValue().toString();
                String image = dataSnapshot.child(UserData.IMAGE_DEFAULT_VALUE).getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                mUserRequestManager.checkIfCurrentUserSelected(mCurrentUser, user_id, mDeclineBtn, mProfileSendReqBtn);

                //--------------- FRIENDS LIST / REQUEST FEATURE -----

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)) { // Selected user exits under "Friend_req" root in database

                            String currentUserId = mCurrentUser.getUid();
                            String requestType = dataSnapshot.child(user_id).child(UserRequest.TYPE).getValue().toString();

                            // Check if request has been received or sent, and adjust button text accordingly
                            mUserRequestManager.checkIfRequestSent(mProfileSendReqBtn, mDeclineBtn, requestType);

                        } else {

                            mUserRequestManager.checkIfFriends(mProfileSendReqBtn, mDeclineBtn, mCurrentUser, user_id);

                        }

                        mProgressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUserRequestManager.manageRequest(ProfileActivity.this, mCurrentUser, user_id, mProfileSendReqBtn, mDeclineBtn);

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "ProfileActivity.onResume()");

    }

    private void startProgressDialog() {

        // Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

    }
}