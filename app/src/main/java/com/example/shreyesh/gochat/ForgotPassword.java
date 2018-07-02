package com.example.shreyesh.gochat;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {


    Toolbar forgetPageToolbar;
    Button sendResetEmail;
    TextView successMessage;
    FirebaseAuth firebaseAuth;
    TextInputLayout emailInput;
    ImageView done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgetPageToolbar = (Toolbar) findViewById(R.id.forgetPasswordToolbar);
        sendResetEmail = (Button) findViewById(R.id.sendResetEmail);
        successMessage = (TextView) findViewById(R.id.sentText);
        done = (ImageView) findViewById(R.id.doneImage);
        emailInput = (TextInputLayout) findViewById(R.id.forgetPasswordEmail);


        setSupportActionBar(forgetPageToolbar);
        getSupportActionBar().setTitle("Forgot Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();


        successMessage.setVisibility(View.GONE);
        done.setVisibility(View.GONE);


        final String email = emailInput.getEditText().getText().toString();

        sendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(emailInput.getEditText().getText().toString())) {
                    Toast.makeText(ForgotPassword.this, "Please fill all email field", Toast.LENGTH_LONG).show();
                    return;

                } else if (!Patterns.EMAIL_ADDRESS.matcher(emailInput.getEditText().getText().toString()).matches()) {
                    Toast.makeText(ForgotPassword.this, "Invalid Email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (AppStatus.getInstance(ForgotPassword.this).isOnline())
                    resetPassword(emailInput.getEditText().getText().toString());
                else
                    Toast.makeText(ForgotPassword.this, "No Internet.Check Network Settings", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void resetPassword(String email) {

        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    successMessage.setVisibility(View.VISIBLE);
                    done.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ForgotPassword.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
