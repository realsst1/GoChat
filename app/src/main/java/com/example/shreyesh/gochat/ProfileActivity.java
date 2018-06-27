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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileDisplayName, profileStatus, profileFriendCount;
    private ImageView profileImage;
    private Button sendRequestButton, declineRequestButton;
    private DatabaseReference databaseReference, friendRequestDatabase, friendsDatabase, notificationDatabase, userRef;
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
        declineRequestButton.setEnabled(false);
        declineRequestButton.setVisibility(View.INVISIBLE);


        //Databases
        final String userID = getIntent().getStringExtra("from_user_id");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        friendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        friendsDatabase = FirebaseDatabase.getInstance().getReference().child("FriendsData");
        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());


        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);


        friendsDatabase.keepSynced(true);
        friendRequestDatabase.keepSynced(true);
        databaseReference.keepSynced(true);
        notificationDatabase.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                profileDisplayName.setText(name);
                profileStatus.setText(status);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.sqavatar)
                        .into(profileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(image).placeholder(R.drawable.sqavatar).into(profileImage);
                            }
                        });


                friendRequestDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userID)) {
                            String type = dataSnapshot.child(userID).child("requestType").getValue().toString();
                            if (type.equals("received")) {
                                sendRequestButton.setText("Accept Friend Request");
                                currentState = "requestReceived";
                                declineRequestButton.setVisibility(View.VISIBLE);
                                declineRequestButton.setEnabled(true);
                            } else if (type.equals("sent")) {
                                sendRequestButton.setText("Cancel Friend Request");
                                currentState = "requestSent";
                                declineRequestButton.setVisibility(View.INVISIBLE);
                                declineRequestButton.setEnabled(false);

                            }
                        } else {
                            friendsDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userID)) {
                                        currentState = "friends";
                                        sendRequestButton.setText("Unfriend");
                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                        declineRequestButton.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        progressDialog.dismiss();

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

        //Friend Request


        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendRequestButton.setEnabled(false);


                if (AppStatus.getInstance(getApplicationContext()).isOnline()) {

                    //Not Friends

                    if (currentState.equals("notFriends") && !userID.equals(currentUser.getUid())) {

                        friendRequestDatabase.child(currentUser.getUid()).child(userID).child("requestType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendRequestDatabase.child(userID).child(currentUser.getUid()).child("requestType").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", currentUser.getUid());
                                                notificationData.put("type", "request");

                                                notificationDatabase.child(userID).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        currentState = "requestSent";
                                                        sendRequestButton.setText("Cancel Friend Request");
                                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                                        declineRequestButton.setEnabled(false);

                                                        Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_LONG).show();
                                                    }
                                                });


                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                                sendRequestButton.setEnabled(true);
                            }
                        });

                    }

                    //Cancel Request

                    if (currentState.equals("requestSent") && !userID.equals(currentUser.getUid())) {

                        friendRequestDatabase.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendRequestDatabase.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                sendRequestButton.setEnabled(true);
                                                currentState = "notFriends";
                                                sendRequestButton.setText("Send Friend Request");
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);

                                                Toast.makeText(ProfileActivity.this, "Request Cancelled", Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }


                    //Accept Request

                    if (currentState.equals("requestReceived") && !userID.equals(currentUser.getUid())) {

                        final String currentDate = DateFormat.getDateInstance().format(new Date());
                        friendsDatabase.child(currentUser.getUid()).child(userID).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendsDatabase.child(userID).child(currentUser.getUid()).child("date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                friendRequestDatabase.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            friendRequestDatabase.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {
                                                                        currentState = "friends";
                                                                        sendRequestButton.setText("Unfriend");
                                                                        declineRequestButton.setVisibility(View.INVISIBLE);
                                                                        declineRequestButton.setEnabled(false);

                                                                        Toast.makeText(ProfileActivity.this, "Request Accepted", Toast.LENGTH_LONG).show();
                                                                    }

                                                                }
                                                            });
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                                sendRequestButton.setEnabled(true);

                                            } else {
                                                Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    });
                                }
                            }
                        });

                    }


                    //Unfriend
                    if (currentState.equals("friends")) {

                        friendsDatabase.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    friendsDatabase.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                currentState = "notFriends";
                                                sendRequestButton.setText("Send Friend Request");
                                                declineRequestButton.setVisibility(View.INVISIBLE);
                                                declineRequestButton.setEnabled(false);
                                            } else {
                                                Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                            sendRequestButton.setEnabled(true);
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    sendRequestButton.setEnabled(true);
                }
            }
        });


    }


    protected void onStart() {
        super.onStart();
        if (currentUser != null)
            userRef.child("online").setValue("true");
    }

    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

}
