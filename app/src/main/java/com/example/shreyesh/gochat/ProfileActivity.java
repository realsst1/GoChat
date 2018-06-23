package com.example.shreyesh.gochat;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileStatus, profileFriendCount;
    private ImageView profileImage;
    private Button sendRequestButton;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileDisplayName = (TextView) findViewById(R.id.profileDisplayName);
        profileStatus = (TextView) findViewById(R.id.profileStatus);
        profileFriendCount = (TextView) findViewById(R.id.friendsCountTextView);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        sendRequestButton = (Button) findViewById(R.id.friendRequestButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Details");
        progressDialog.setMessage("Please wait while we load details...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();


        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(getIntent().getStringExtra("user_id"));

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileDisplayName.setText(name);
                profileStatus.setText(status);
                Picasso.get().load(image).placeholder(R.drawable.sqavatar).into(profileImage);

                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
