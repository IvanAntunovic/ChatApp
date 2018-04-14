package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn;

    private ProgressDialog mProgressDialog;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrentUser;

    private String mCurrentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra(IntentData.USERS_TO_PROFILE_PICTURE_USER_ID);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_send_req_btn);

        mCurrentState = UserRequest.NOT_FRIENDS;

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while we load user data.");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child(UserData.NAME_DEFAULT_VALUE).getValue().toString();
                String status = dataSnapshot.child(UserData.STATUS_DEFAULT_VALUE).getValue().toString();
                String image = dataSnapshot.child(UserData.IMAGE_DEFAULT_VALUE).getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(status);

                // Friends list / Request Feature
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)) {

                            String requestType = dataSnapshot.child(userId).child(UserRequest.TYPE).toString();

                            if (requestType.equals(UserRequest.RECEIVED)) {

                                mCurrentState = UserRequest.REQUEST_RECEIVED;
                                mProfileSendReqBtn.setText("Accept Friend Request");

                            } else if (requestType.equals(UserRequest.SENT)){

                                mCurrentState = UserRequest.SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                            }

                        }

                        mProgressDialog.dismiss();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Not friends state
                if (mCurrentState.equals(UserRequest.NOT_FRIENDS)) {

                    // Disable tapping the button
                    mProfileSendReqBtn.setEnabled(false);

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId).child(UserRequest.TYPE)
                            .setValue(UserRequest.SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                // Enable tapping the button
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrentState = UserRequest.SENT;
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid()).child(UserRequest.TYPE)
                                        .setValue(UserRequest.RECEIVED).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(ProfileActivity.this, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();

                                    }
                                });

                            } else {

                                Toast.makeText(ProfileActivity.this, "Failed Sending Request.", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
                // Cancel request state
                else if (mCurrentState.equals(UserRequest.SENT)) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mFriendRequestDatabase.child(userId).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    // Enable tapping the button
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrentState = UserRequest.NOT_FRIENDS;
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                }
                            });
                        }
                    });
                }
            }
        });

    }
}
