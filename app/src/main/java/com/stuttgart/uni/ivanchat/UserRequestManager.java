package com.stuttgart.uni.ivanchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class UserRequestManager {

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private FirebaseUser mCurrentUser;

    private Button mProfileSendReqBtn;

    public static final int RECEIVED = 0;
    public static final int SENT = 1;
    public static final int TYPE = 2;
    public static final int NOT_FRIENDS = 3;
    public static final int REQUEST_RECEIVED = 4;

    private int mCurrentState;

    public UserRequestManager(DatabaseReference mUsersDatabase, DatabaseReference mFriendRequestDatabase, FirebaseUser mCurrentUser, Button mProfileSendReqBtn) {

        this.mUsersDatabase = mUsersDatabase;
        this.mFriendRequestDatabase = mFriendRequestDatabase;
        this.mCurrentUser = mCurrentUser;
        this.mProfileSendReqBtn = mProfileSendReqBtn;

        mCurrentState = this.NOT_FRIENDS;

    }

    public void manage(final Context context, final String targetUserId) {

        // Not friends state
        if (mCurrentState == NOT_FRIENDS) {

            // Disable tapping the button
            mProfileSendReqBtn.setEnabled(false);

            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(targetUserId).child(UserRequest.TYPE)
                    .setValue(UserRequest.SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        // Enable tapping the button
                        mProfileSendReqBtn.setEnabled(true);
                        mCurrentState = SENT;
                        mProfileSendReqBtn.setText("Cancel Friend Request");

                        mFriendRequestDatabase.child(targetUserId).child(mCurrentUser.getUid()).child(UserRequest.TYPE)
                                .setValue(UserRequest.RECEIVED).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(context, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();

                            }
                        });

                    } else {

                        Toast.makeText(context, "Failed Sending Request.", Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
        // Cancel request state
        else if (mCurrentState == SENT) {

            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(targetUserId)
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    mFriendRequestDatabase.child(targetUserId).child(mCurrentUser.getUid())
                            .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // Enable tapping the button
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrentState = NOT_FRIENDS;
                            mProfileSendReqBtn.setText("Send Friend Request");

                        }
                    });
                }
            });
        }
    }
}
