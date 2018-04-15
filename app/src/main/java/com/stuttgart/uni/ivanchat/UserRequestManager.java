package com.stuttgart.uni.ivanchat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserRequestManager {

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    public static final int RECEIVED = 0;
    public static final int REQUEST_SENT = 1;
    public static final int TYPE = 2;
    public static final int NOT_FRIENDS = 3;
    public static final int FRIENDS = 4;
    public static final int REQUEST_RECEIVED = 5;


    private int mCurrentState;

    public UserRequestManager(DatabaseReference usersDatabase,
                              DatabaseReference friendRequestDatabase,
                              DatabaseReference friendDatabase,
                              DatabaseReference notificationDatabase) {

        this.mUsersDatabase = usersDatabase;
        this.mFriendRequestDatabase = friendRequestDatabase;
        this.mFriendDatabase = friendDatabase;
        this.mNotificationDatabase = notificationDatabase;

        mCurrentState = this.NOT_FRIENDS;

    }

    public void manageRequest(final Context context, final FirebaseUser currentUser, final String targetUserId, final Button profileSendReqBtn, final Button declineBtn) {

        // Not friends state
        switch (mCurrentState) {

            case NOT_FRIENDS: {

                // Disable tapping the button
                profileSendReqBtn.setEnabled(false);

                mFriendRequestDatabase.child(currentUser.getUid()).child(targetUserId).child(UserRequest.TYPE)
                        .setValue(UserRequest.SENT).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mCurrentState = REQUEST_SENT;
                            profileSendReqBtn.setText("Cancel Friend Request");

                            mFriendRequestDatabase.child(targetUserId).child(currentUser.getUid()).child(UserRequest.TYPE)
                                    .setValue(UserRequest.RECEIVED).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    // Send notificaction
                                    HashMap<String, String> notificationData = new HashMap<>();
                                    notificationData.put(UserData.REQUEST_FROM, currentUser.getUid());
                                    notificationData.put(UserData.REQUEST_TYPE, "request");

                                    mNotificationDatabase.child(targetUserId).push()
                                            .setValue(notificationData)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    mNotificationDatabase.child(targetUserId).push();

                                                    declineBtn.setVisibility(View.INVISIBLE);
                                                    declineBtn.setEnabled(false);

                                                    Toast.makeText(context, "Request Sent Successfully.", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                            });

                        } else {

                            Toast.makeText(context, "Failed Sending Request.", Toast.LENGTH_SHORT).show();

                        }

                        // Enable tapping the button
                        profileSendReqBtn.setEnabled(true);
                    }
                });
                break;
            }
            // Cancel request state
            case REQUEST_SENT: {

                this.removeRequest(profileSendReqBtn, "Send Friend Request", currentUser, targetUserId);
                mCurrentState = NOT_FRIENDS;

                declineBtn.setVisibility(View.INVISIBLE);
                declineBtn.setEnabled(false);

            }
            break;

            case REQUEST_RECEIVED: {

                final String currentDate = DateFormat.getDateInstance().format(new Date());

                mFriendDatabase.child(currentUser.getUid()).child(targetUserId).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        // Enable tapping the button
                        profileSendReqBtn.setEnabled(true);

                        mFriendDatabase.child(targetUserId).child(currentUser.getUid()).setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                removeRequest(profileSendReqBtn, "Unfriend this Person" ,currentUser, targetUserId);
                                mCurrentState = FRIENDS;

                                declineBtn.setVisibility(View.INVISIBLE);
                                declineBtn.setEnabled(false);

                            }
                        });
                    }
                });

            }
            break;

            default:
                break;
        }

    }

    private void removeRequest(final Button profileSendReqBtn, final String buttonText, final FirebaseUser currentUser, final String targetUserId) {

        mFriendRequestDatabase.child(currentUser.getUid()).child(targetUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                mFriendRequestDatabase.child(targetUserId).child(currentUser.getUid())
                        .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Enable tapping the button
                        profileSendReqBtn.setEnabled(true);
                        profileSendReqBtn.setText(buttonText);

                        // Enable tapping the button
                        profileSendReqBtn.setEnabled(true);
                    }
                });
            }
        });

    }

    public void manageButton(Button requestButton, Button declineBtn, String requestType) {

        if (requestType.equals(UserRequest.RECEIVED)) {

            mCurrentState = REQUEST_RECEIVED;
            requestButton.setText("Accept Friend Request");

            declineBtn.setVisibility(View.VISIBLE);
            declineBtn.setEnabled(true);

        } else if (requestType.equals(UserRequest.SENT)){

            mCurrentState = REQUEST_SENT;
            requestButton.setText("Cancel Friend Request");

            declineBtn.setVisibility(View.INVISIBLE);
            declineBtn.setEnabled(false);

        }

    }

    public void checkIfFriends(final Button mProfileSendReqBtn, final FirebaseUser mCurrentUser, final String userId) {

        mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check whether the current profile we are on, exists or not
                if (dataSnapshot.hasChild(userId)) {

                    // if it's true, current user is already friend with the current profile we are on
                    mCurrentState = FRIENDS;
                    mProfileSendReqBtn.setText("Unfriend this Person");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
