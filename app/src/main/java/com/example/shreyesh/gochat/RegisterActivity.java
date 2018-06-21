package com.example.shreyesh.gochat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout regDisplayName, regEmail, regPassword;
    private Button regCreateButton;
    private Toolbar regPageToolbar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupUIViews();
        firebaseAuth = FirebaseAuth.getInstance();


        regPageToolbar = (Toolbar) findViewById(R.id.regPageToolbar);
        setSupportActionBar(regPageToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);


        regCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getEditText().getText().toString();
                String name = regDisplayName.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Invalid email", Toast.LENGTH_LONG).show();
                    return;
                } else if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password length should atleast be 8", Toast.LENGTH_LONG).show();
                    return;
                }
                progressDialog.setTitle("Registering User");
                progressDialog.setMessage("Please wait while we register you..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                registerUser(name, email, password);
            }
        });
    }

    private void registerUser(final String name, String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userID = currentUser.getUid();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("status", "Hi there, I'm using GoChat! ");
                    userMap.put("image", "default");
                    userMap.put("thumbnail", "default");

                    databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Registration Succesful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finish();
                            }

                        }
                    });
                } else {
                    progressDialog.hide();
                    Toast.makeText(RegisterActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupUIViews() {
        regDisplayName = (TextInputLayout) findViewById(R.id.regDisplayName);
        regEmail = (TextInputLayout) findViewById(R.id.regEmail);
        regPassword = (TextInputLayout) findViewById(R.id.regPassword);
        regCreateButton = (Button) findViewById(R.id.regCreateAccountButton);

    }
}
