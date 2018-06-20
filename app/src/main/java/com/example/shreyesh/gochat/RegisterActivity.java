package com.example.shreyesh.gochat;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout regDisplayName, regEmail, regPassword;
    private Button regCreateButton;
    private Toolbar regPageToolbar;
    private FirebaseAuth firebaseAuth;

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

        regCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = regEmail.getEditText().getText().toString();
                String name = regDisplayName.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                registerUser(name, email, password);


            }
        });
    }

    private void registerUser(String name, String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "Registration Succesful", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
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
