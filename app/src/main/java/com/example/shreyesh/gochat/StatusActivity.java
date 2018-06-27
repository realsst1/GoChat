package com.example.shreyesh.gochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class StatusActivity extends AppCompatActivity {


    private Toolbar statusPageToolbar;
    private TextInputLayout statusInputField;
    private Button saveButton;
    private DatabaseReference databaseReference, userRef;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        statusPageToolbar = (Toolbar) findViewById(R.id.statusPageToolbar);
        setSupportActionBar(statusPageToolbar);
        getSupportActionBar().setTitle("Change Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        statusInputField = (TextInputLayout) findViewById(R.id.changeStatusInputField);
        saveButton = (Button) findViewById(R.id.statusSaveButton);
        userRef = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        String status = getIntent().getStringExtra("status");

        statusInputField.getEditText().setText(status);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);


            if (AppStatus.getInstance(this).isOnline()) {

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        progressDialog = new ProgressDialog(StatusActivity.this);
                        progressDialog.setTitle("Saving Changes");
                        progressDialog.setMessage("Please wait while we save changes... ");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        String status = statusInputField.getEditText().getText().toString();
                        databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                } else {
                                    progressDialog.hide();
                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    }
                });
            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }

    }

    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef.child("online").setValue("true");
        }
    }

    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }
}
