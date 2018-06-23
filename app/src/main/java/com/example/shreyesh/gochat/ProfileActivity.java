package com.example.shreyesh.gochat;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileStatus, profileFriendCount;
    private ImageView profileImage;
    private Button sendRequestButton, declineRequestButton;
    private DatabaseReference databaseReference, friendRequestDatabase;
    private ProgressDialog progressDialog;
    private String currentState;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Initialize values
        profileDisplayName = (TextView) findViewById(R.id.profileDisplayName);
        profileStatus = (TextView) findViewById(R.id.profileStatus);
        profileFriendCount = (TextView) findViewById(R.id.friendsCountTextView);
        profileImage = (ImageView) findViewById(R.id.profileImage);
        sendRequestButton = (Button) findViewById(R.id.friendRequestButton);
        declineRequestButton = (Button) findViewById(R.id.declineFriendRequest);

        //ProgressBar
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Details");
        progressDialog.setMessage("Please wait while we load details...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        currentState = "notFriends";

        final String userID = getIntent().getStringExtra("user_id");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

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

        //Friend Request
        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState.equals("notFriends")) {
                    friendRequestDatabase.child(currentUser.getUid()).child(userID).child("requestType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                friendRequestDatabase.child(userID).child(currentUser.getUid()).child("requestType").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            }
        });






    }
}
