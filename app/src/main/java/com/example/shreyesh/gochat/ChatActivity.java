package com.example.shreyesh.gochat;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatActivity extends AppCompatActivity {

    private String chatUser, chatUserName;
    private Toolbar chatToolbar;
    private DatabaseReference databaseReference;

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
        getSupportActionBar().setTitle(chatUserName);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_app_bar_layout, null);

        actionBar.setCustomView(view);




    }
}
