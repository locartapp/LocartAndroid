package com.locart.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.locart.Utils.Constants;
import com.locart.Utils.DataProccessor;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import com.locart.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText mEmail;
    DataProccessor dataProccessor;
    private ProgressBar progressBar;
    String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mEmail = findViewById(R.id.email);
        progressBar = findViewById(R.id.progressBar);
        dataProccessor = new DataProccessor(this);

        Intent intent = getIntent();
        pass = intent.getStringExtra("forgot_pass");

        forgotPassword();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });
    }

    private void forgotPassword() {
        /* send reset email to the provided email or show error */
        findViewById(R.id.forgotPassword).setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            if (mEmail.getText().length() == 0) {
                progressBar.setVisibility(View.GONE);
                mEmail.setError("Please enter email");
                return;
            }

            String checkEmail = dataProccessor.getStr(Constants.EMAIL);
            /* hide keyboard layout */
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            if (pass.equals("settings")) {
                /* check if user mail match email enter in edit text field */
                if (checkEmail.equals(mEmail.getText().toString())) {

                    FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail.getText().toString())
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Password reset or verification mail sent",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }

                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "This Email is not associated with this account",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (pass.equals("login")) {
                /* No need to check if email is in shared preference.
                 * check if email contain necessary details */
                if (isValidEmail(mEmail.getText().toString())) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail.getText().toString())
                            .addOnCompleteListener(task -> {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(ForgotPasswordActivity.this, "Password reset or verification mail sent",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Toast.makeText(ForgotPasswordActivity.this, "Error: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }

                            });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ForgotPasswordActivity.this, "Email is not valid", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}