package com.example.shreyesh.gochat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference databaseReference, userRef;
    private CircleImageView circleImageView;
    private Button changeImage, changeStatus;
    private TextView displayName, userStatus;
    private FirebaseUser currentUser;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUIViews();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        databaseReference.keepSynced(true);
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String displayImage = dataSnapshot.child("image").getValue().toString();
                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                displayName.setText(name);
                userStatus.setText(status);

                if (!displayImage.equals("default")) {

                    Picasso.get().load(displayImage).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.avatarplaceholder)
                            .into(circleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(displayImage).placeholder(R.drawable.avatarplaceholder).into(circleImageView);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = userStatus.getText().toString();
                startActivity(new Intent(SettingsActivity.this, StatusActivity.class).putExtra("status", status));
            }
        });


        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .setMinCropResultSize(512, 512)
                        .start(SettingsActivity.this);

            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

    private void setupUIViews() {
        circleImageView = (CircleImageView) findViewById(R.id.settingsImage);
        changeImage = (Button) findViewById(R.id.settingsChangeImageButton);
        changeStatus = (Button) findViewById(R.id.settingsChangeStatusButton);
        displayName = (TextView) findViewById(R.id.settingsDisplayName);
        userStatus = (TextView) findViewById(R.id.settingsStatusTextView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while we upload the image...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userID = currentUser.getUid();

                File thumnbailFile = new File(resultUri.getPath());
                Bitmap thumbnail = null;

                try {
                    thumbnail = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumnbailFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                final byte[] dataValue = byteArrayOutputStream.toByteArray();

                final StorageReference thumbPath = storageReference.child("profile_pictures").child("thumbs").child(userID + ".jpg");

                StorageReference filepath = storageReference.child("profile_pictures").child(userID + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            final String downloadURI = task.getResult().getDownloadUrl().toString();
                            UploadTask uploadTask = thumbPath.putBytes(dataValue);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        String thumbURI = task.getResult().getDownloadUrl().toString();
                                        Map hashMap = new HashMap<>();
                                        hashMap.put("image", downloadURI);
                                        hashMap.put("thumbnail", thumbURI);

                                        databaseReference.updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.hide();
                                                    Toast.makeText(SettingsActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                    } else {
                                        progressDialog.hide();
                                        Toast.makeText(SettingsActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG);
                                    }
                                }
                            });


                        } else {
                            progressDialog.hide();
                            Toast.makeText(SettingsActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
