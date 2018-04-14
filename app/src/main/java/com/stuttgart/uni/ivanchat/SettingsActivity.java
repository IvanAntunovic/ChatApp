package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

        private static final int GALLERY_PICK = 1;
        private static final String TAG = "IvanMessage";

        private DatabaseReference mUserDatabase;

        private static final String ROOT_DIR = "Users";
        private static final String NAME = "name";
        private static final String IMAGE = "image";
        private static final String STATUS = "status";
        private static final String THUMB_IMAGE = "thumb_image";

        private CircleImageView mDisplayImage;
        private TextView mDisplayName;
        private TextView mDisplayStatus;
        private Button mStatusButtton;
        private Button mImageButton;
        private ImageView mChangePicture;

        private String mProfileImage;

        private FirebaseUser mCurrentUser;

        // Storage Firebase
        private StorageReference mImageStorage;

        private ProgressDialog mProgressDialog;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);
            mDisplayName = (TextView) findViewById(R.id.settings_name);
            mDisplayStatus = (TextView) findViewById(R.id.settings_status);
            mChangePicture = (ImageView) findViewById(R.id.settings_change_picture);

            mStatusButtton = (Button) findViewById(R.id.settings_status_btn);
            mImageButton = (Button) findViewById(R.id.settings_image_btn);

            // Firebase storage
            mImageStorage = FirebaseStorage.getInstance().getReference();

            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

            String current_uid = null;
            if (mCurrentUser != null) {

                current_uid = mCurrentUser.getUid();

            }

            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
            mUserDatabase.keepSynced(true);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name = dataSnapshot.child("name").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    final String image = dataSnapshot.child("image").getValue().toString();
                    String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                    mDisplayName.setText(name);
                    mDisplayStatus.setText(status);

                    // Update ImageView with new profile image
                    if(!image.equals("default")) {

                        //Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                        mProfileImage = image;

                        Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);

                        }
                    });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mStatusButtton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {

                String currentUserStatusValue = null;

                if (mDisplayStatus != null) {

                    currentUserStatusValue = mDisplayStatus.getText().toString();
                    startStatusActivity(currentUserStatusValue);

                }
                }
            });

            mDisplayStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String currentUserStatusValue = null;

                    if (mDisplayStatus != null) {

                        currentUserStatusValue = mDisplayStatus.getText().toString();
                        startStatusActivity(currentUserStatusValue);

                    }

                }
            });

            mImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startGalleryActivity();

                }
            });

            mDisplayImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startProfilePictureActivity();

                }
            });

            mDisplayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String currentUserDisplayName = null;

                    if (mDisplayName != null) {

                        currentUserDisplayName = mDisplayName.getText().toString();

                    }

                    startDisplayNameActivity(currentUserDisplayName);

                }
            });

            mChangePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startGalleryActivity();

                }
            });
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

                Uri imageUri = data.getData();

                CropImage.activity(imageUri)
                        .setAspectRatio(1, 1)
                        .setMinCropWindowSize(500, 500)
                        .start(this);

            }


            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    this.showProgressDialog();

                    Uri resultUri = result.getUri();

                    File thumb_filePath = new File(resultUri.getPath());

                    String current_user_id = mCurrentUser.getUid();

                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                    final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if(task.isSuccessful()){

                                final String download_url = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        if(thumb_task.isSuccessful()){

                                            Map update_hashMap = new HashMap();
                                            update_hashMap.put("image", download_url);
                                            update_hashMap.put("thumb_image", thumb_downloadUrl);

                                            mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if(task.isSuccessful()){

                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(SettingsActivity.this, "Success Uploading.", Toast.LENGTH_LONG).show();

                                                    }

                                                }
                                            });

                                        } else {

                                            Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                            mProgressDialog.dismiss();

                                        }


                                    }
                                });

                            } else {

                                Toast.makeText(SettingsActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                                mProgressDialog.dismiss();

                            }
                        }
                    });

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                    Exception error = result.getError();

                }
            }
        }

        private void showProgressDialog() {

            mProgressDialog = new ProgressDialog(SettingsActivity.this);
            mProgressDialog.setTitle("Uploading image...");
            mProgressDialog.setMessage("Please wait while we upload and process image");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();

        }

        private void startStatusActivity(String currentUserStatus) {

            final String STATUS_KEY_VALUE = "status_value";

            Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
            statusIntent.putExtra(STATUS_KEY_VALUE, currentUserStatus);
            super.startActivity(statusIntent);
            super.finish();
        }

        private void startGalleryActivity() {

            Intent galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

        }

        private void startDisplayNameActivity(String currentUserDisplayName) {

            final String USER_NAME_KEY_VALUE = "user_name_value";

            Intent displayNameIntent = new Intent(SettingsActivity.this, DisplayNameActivity.class);
            displayNameIntent.putExtra(USER_NAME_KEY_VALUE, currentUserDisplayName);
            super.startActivity(displayNameIntent);
            super.finish();
        }

        private void startProfilePictureActivity() {

            Intent profilePictureIntent = new Intent(SettingsActivity.this, ProfilePictureActivity.class);
            profilePictureIntent.putExtra(IntentData.SETTINGS_TO_PROFILE_PICTURE_DISPLAY_IMAGE, mProfileImage);
            super.startActivity(profilePictureIntent);

        }

        private void storeImage(Uri resultUri) {

            String currentUserId = mCurrentUser.getUid();

            File thumb_filePath = new File(resultUri.getPath());

            String current_user_id = mCurrentUser.getUid();

            Bitmap thumb_bitmap = new Compressor(this)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(thumb_filePath);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] thumb_byte = baos.toByteArray();

            StorageReference filePath = mImageStorage.child("profile_images").child(currentUserId + ".jpg");
            final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                if(thumb_task.isSuccessful()){

                                    Map update_hashMap = new HashMap();
                                    update_hashMap.put("image", download_url);
                                    update_hashMap.put("thumb_image", thumb_downloadUrl);

                                    mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                mProgressDialog.dismiss();
                                                Toast.makeText(SettingsActivity.this, "Success Uploading.", Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });

                                } else {

                                    Toast.makeText(SettingsActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                    mProgressDialog.dismiss();

                                }
                            }
                        });

                    } else {

                        Toast.makeText(SettingsActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                        mProgressDialog.dismiss();

                    }
                }
            });
    }
}

