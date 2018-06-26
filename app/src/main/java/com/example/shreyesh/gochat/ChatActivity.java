package com.example.shreyesh.gochat;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, chatUserName;
    private Toolbar chatToolbar;
    private DatabaseReference databaseReference;
    private TextView displayName, lastSeen;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatUser = getIntent().getStringExtra("from_user_id");
        chatUserName = getIntent().getStringExtra("user_name");
        databaseReference = FirebaseDatabase.getInstance().getReference();


        if (chatUser == null)
            Toast.makeText(ChatActivity.this, "UserID NULL", Toast.LENGTH_LONG).show();

        chatToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        ActionBar actionBar;
        setSupportActionBar(chatToolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);


        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_app_bar_layout, null);

        actionBar.setCustomView(view);


        displayName = (TextView) findViewById(R.id.chatBarDisplayName);
        lastSeen = (TextView) findViewById(R.id.chatBarLastSeen);
        profileImage = (CircleImageView) findViewById(R.id.chatAppbarImage);

        displayName.setText(chatUserName);

        databaseReference.keepSynced(true);

        databaseReference.child("users").child(chatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("thumbnail").getValue().toString();

                if (online.equals("true")) {
                    lastSeen.setText("online");
                } else {
                    long time = Long.parseLong(online);
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    String date = getTimeAgo.getTimeAgo(time, getApplicationContext());
                    lastSeen.setText("last seen at " + date);
                }

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatarplaceholder).into(profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(image).placeholder(R.drawable.avatarplaceholder).into(profileImage);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
