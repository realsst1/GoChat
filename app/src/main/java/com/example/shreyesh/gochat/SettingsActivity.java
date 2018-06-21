package com.example.shreyesh.gochat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseReference databaseReference;
    private CircleImageView circleImageView;
    private Button changeImage, changeStatus;
    private TextView displayName, userStatus;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUIViews();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String displayImage = dataSnapshot.child("image").getValue().toString();
                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                displayName.setText(name);
                userStatus.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setupUIViews() {
        circleImageView = (CircleImageView) findViewById(R.id.settingsImage);
        changeImage = (Button) findViewById(R.id.settingsChangeImageButton);
        changeStatus = (Button) findViewById(R.id.settingsChangeStatusButton);
        displayName = (TextView) findViewById(R.id.settingsDisplayName);
        userStatus = (TextView) findViewById(R.id.settingsStatusTextView);
    }
}
