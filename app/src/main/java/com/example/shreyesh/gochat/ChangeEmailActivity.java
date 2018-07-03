package com.example.shreyesh.gochat;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmailActivity extends AppCompatActivity {

    private TextInputLayout currentEmail, currentPassword, newEmail;
    private Button verfiy, saveChanges;
    private Toolbar changeEmailPageToolbar;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog dialogVerify, dialogSaveChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        setupUIViews();
        setSupportActionBar(changeEmailPageToolbar);
        getSupportActionBar().setTitle("Change Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        dialogSaveChanges = new ProgressDialog(this);
        dialogVerify = new ProgressDialog(this);


        verfiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (TextUtils.isEmpty(currentEmail.getEditText().getText().toString()) || TextUtils.isEmpty(currentPassword.getEditText().getText().toString())) {
                    Toast.makeText(ChangeEmailActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(currentEmail.getEditText().getText().toString()).matches()) {
                    Toast.makeText(ChangeEmailActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!AppStatus.getInstance(ChangeEmailActivity.this).isOnline()) {
                    Toast.makeText(ChangeEmailActivity.this, "No Internet.Check network settings", Toast.LENGTH_LONG).show();
                    return;
                }


                dialogVerify.setTitle("Verifying");
                dialogVerify.setMessage("Please wait while we reauthenticate your credentials...");
                dialogVerify.setCanceledOnTouchOutside(false);
                dialogVerify.show();

                FirebaseUser user = firebaseAuth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(currentEmail.getEditText().getText().toString(), currentPassword.getEditText().getText().toString());

                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            dialogVerify.dismiss();
                            currentEmail.setVisibility(View.GONE);
                            currentPassword.setVisibility(View.GONE);
                            verfiy.setVisibility(View.GONE);

                            saveChanges.setVisibility(View.VISIBLE);
                            newEmail.setVisibility(View.VISIBLE);

                            final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                            final String newAddress = newEmail.getEditText().getText().toString();
                        } else {
                            dialogVerify.hide();
                            Toast.makeText(ChangeEmailActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty((newEmail.getEditText().getText().toString()))) {
                    Toast.makeText(ChangeEmailActivity.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                    return;
                } else if (!Patterns.EMAIL_ADDRESS.matcher((newEmail.getEditText().getText().toString())).matches()) {
                    Toast.makeText(ChangeEmailActivity.this, "Invalid Email", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!AppStatus.getInstance(ChangeEmailActivity.this).isOnline()) {
                    Toast.makeText(ChangeEmailActivity.this, "No Internet.Check network settings", Toast.LENGTH_LONG).show();
                    return;
                }

                dialogSaveChanges.setTitle("Saving Changes");
                dialogSaveChanges.setMessage("Please wait while we update your email...");
                dialogSaveChanges.setCanceledOnTouchOutside(false);
                dialogSaveChanges.show();


                currentUser.updateEmail(newEmail.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            dialogSaveChanges.dismiss();
                            Toast.makeText(ChangeEmailActivity.this, "Email Updated Successfully", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ChangeEmailActivity.this, MainActivity.class));
                            finish();
                        } else {
                            dialogSaveChanges.hide();
                            Toast.makeText(ChangeEmailActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }

    private void setupUIViews() {
        currentEmail = (TextInputLayout) findViewById(R.id.changeEmailCurrentEmail);
        currentPassword = (TextInputLayout) findViewById(R.id.changeEmailPassword);
        newEmail = (TextInputLayout) findViewById(R.id.newEmail);
        verfiy = (Button) findViewById(R.id.verify);
        saveChanges = (Button) findViewById(R.id.saveNewEmail);
        changeEmailPageToolbar = (Toolbar) findViewById(R.id.changeEmailPageToolbar);

        newEmail.setVisibility(View.GONE);
        saveChanges.setVisibility(View.GONE);
    }
}
