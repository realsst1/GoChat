package com.example.shreyesh.gochat;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private Toolbar loginPageToolbar;
    private TextInputLayout loginEmail, loginPassword;
    private Button loginButtonLoginPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupUIViews();
        loginPageToolbar = (Toolbar) findViewById(R.id.loginPageToolbar);
        setSupportActionBar(loginPageToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupUIViews() {
        loginEmail = (TextInputLayout) findViewById(R.id.loginEmail);
        loginPassword = (TextInputLayout) findViewById(R.id.loginPassword);
        loginButtonLoginPage = (Button) findViewById(R.id.loginButton);
    }
}
