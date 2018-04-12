package com.stuttgart.uni.ivanchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;

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
                String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mDisplayStatus.setText(status);

                // Update ImageView with new profile image
                Picasso.get().load(image).into(mDisplayImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mStatusButtton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                String currentUserStatusValue = mDisplayStatus.getText().toString();

                startStatusActivity(currentUserStatusValue);

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

                startGalleryActivity();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            // Cropping the image
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                showProgressDialog();

                this.storeImage(result.getUri());

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
    }

    private void startGalleryActivity() {

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

    }

    private void storeImage(Uri resultUri) {

        String currentUserId = mCurrentUser.getUid();

        StorageReference filePath = mImageStorage.child("profile_images").child(currentUserId + ".jpg");

        filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    String downloadUrl = task.getResult().getDownloadUrl().toString();
                    mUserDatabase.child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            mProgressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Picture has been uploaded successfully", Toast.LENGTH_LONG).show();

                        }
                    });

                } else {

                    Toast.makeText(SettingsActivity.this, "Picture has not been upload successfully", Toast.LENGTH_LONG).show();
                    mProgressDialog.dismiss();
                }
            }
        });

    }
}
