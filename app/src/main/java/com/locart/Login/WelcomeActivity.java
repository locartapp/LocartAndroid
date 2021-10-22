package com.locart.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.locart.R;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{

    Button mLogin, mRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        initializeObjects();
    }

    /**
     * initializes the design Elements
     */
    private void initializeObjects(){
        mLogin = findViewById(R.id.login);
        mRegistration = findViewById(R.id.registration);

        mRegistration.setOnClickListener(this);
        mLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registration:
                registrationClick();
                break;
            case R.id.login:
                loginClick();
                break;
        }
    }

    private void loginClick() {
        Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(login);
    }

    private void registrationClick() {
        Intent login = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(login);
    }
}