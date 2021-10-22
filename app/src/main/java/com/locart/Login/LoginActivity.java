package com.locart.Login;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import com.locart.Activity.MainActivity;
import com.locart.R;

public class LoginActivity extends Activity implements View.OnClickListener{

    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mForgotButton;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    private ProgressBar progressBar;
    String userId;
    private static final String TAG = LoginActivity.class.getSimpleName();
    DataProccessor dataProccessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initializeObjects();
    }

    /**
     * Initializes the design Elements and calls clickListeners for them
     */
    private void initializeObjects() {
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mForgotButton = findViewById(R.id.forgotButton);
        mLogin = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();
        mForgotButton.setOnClickListener(this);
        mLogin.setOnClickListener(this);
        dataProccessor = new DataProccessor(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgotButton:
                forgotPassword();
                break;
            case R.id.login:
                login();
                break;
        }
    }

    /**
     * Go to forgot password activity
     */
    private void forgotPassword() {
        /* go to forgot password activity */
        Intent forgotPassword = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        forgotPassword.putExtra("forgot_pass", "login");
        startActivity(forgotPassword);

    }

    /**
     * Logs in the user
     */
    private void login() {
        progressBar.setVisibility(View.VISIBLE);

        if (mEmail.getText().length() == 0) {
            progressBar.setVisibility(View.GONE);
            mEmail.setError("Please enter email");
            return;
        }
        if (mPassword.getText().length() == 0) {
            progressBar.setVisibility(View.GONE);
            mPassword.setError("Please enter password");
            return;
        }

        mLogin.setClickable(false);
        final String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();
        /* hide keyboard layout */
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        assert inputManager != null;
        inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dataProccessor.setStr(Constants.PASSWORD, password);
                        checkIfEmailVerified();
                    }
                    else {
                        progressBar.setVisibility(View.GONE);
                        mLogin.setClickable(true);
                        Toast.makeText(LoginActivity.this, "User Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void checkIfEmailVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        progressBar.setVisibility(View.GONE);
        userId = user.getUid();

        if (user.isEmailVerified()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            FirebaseAuth.getInstance().signOut();
            mLogin.setClickable(true);
//            Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
            /* show alert dialog */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Verify Email");
            builder.setMessage("Please verify your email");

            /* add a button */
            builder.setPositiveButton("DISMISS", (dialog, which) -> {

            });

            /* create and show the alert dialog */
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

        }
    }

}