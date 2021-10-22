package com.locart;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.locart.Login.WelcomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.locart.Activity.MainActivity;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        Intent intent;
        if (user != null && user.isEmailVerified()) {
//                Log.d("Splash", "main activity");
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }else {
//                Log.d("Splash", "no user");
            intent = new Intent(SplashActivity.this, WelcomeActivity.class);
        }
        startActivity(intent);
        finish();

    }

}
