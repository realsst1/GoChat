package com.example.shreyesh.gochat;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, chatUserName, currentUserID;
    private Toolbar chatToolbar;
    private DatabaseReference databaseReference, userRef;
    private TextView displayName, lastSeen;
    private CircleImageView profileImage;
    private FirebaseUser currentUser;
    private FirebaseAuth firebaseAuth;

    private TextView sendMessageText;
    private ImageView sendFilesButton, sendMessageButton;


    private RecyclerView messageList;


    private List<Messages> messagesListView;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        messagesListView = new ArrayList<>();
        messageAdapter = new MessageAdapter(messagesListView);

        //Initialize values
        chatUser = getIntent().getStringExtra("from_user_id");
        chatUserName = getIntent().getStringExtra("user_name");
        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();


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

        //Chat Activity App bar
        displayName = (TextView) findViewById(R.id.chatBarDisplayName);
        lastSeen = (TextView) findViewById(R.id.chatBarLastSeen);
        profileImage = (CircleImageView) findViewById(R.id.chatAppbarImage);


        //Chat Activity Views
        sendFilesButton = (ImageView) findViewById(R.id.addFilesImageButton);
        sendMessageButton = (ImageView) findViewById(R.id.sendMessageButton);
        sendMessageText = (EditText) findViewById(R.id.sendMessageText);
        messageList = (RecyclerView) findViewById(R.id.messageList);

        layoutManager = new LinearLayoutManager(this);
        messageList.setLayoutManager(layoutManager);
        messageList.setAdapter(messageAdapter);

        displayName.setText(chatUserName);

        databaseReference.keepSynced(true);


        loadMessages();

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

                    lastSeen.setText("last seen " + date);

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


        databaseReference.child("chats").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(chatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("chats/" + currentUserID + "/" + chatUser, chatAddMap);
                    chatUserMap.put("chats/" + chatUser + "/" + currentUserID, chatAddMap);


                    databaseReference.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {

                            }
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                sendMessageText.setText("");
            }
        });


    }

    private void loadMessages() {

        databaseReference.child("messages").child(currentUserID).child(chatUser).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesListView.add(messages);
                messageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = sendMessageText.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String currentUserRef = "messages/" + currentUserID + "/" + chatUser;
            String chatUserRef = "messages/" + chatUser + "/" + currentUserID;

            DatabaseReference userMessagePush = databaseReference.child("messages").child(currentUserID).child(chatUser).push();

            String pushID = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUserRef + "/" + pushID, messageMap);
            messageUserMap.put(chatUserRef + "/" + pushID, messageMap);


            databaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {

                    }
                }
            });

        }

    }


    //Online Feature
    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

    protected void onResume() {
        super.onResume();
        if (currentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

    protected void onPause() {
        super.onPause();
        if (currentUser != null) {
            userRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }


}
