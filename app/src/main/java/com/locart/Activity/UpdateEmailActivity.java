package com.locart.Activity;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.locart.Locart;
import com.locart.Login.WelcomeActivity;
import com.locart.Utils.ConfirmPasswordDialog;
import com.locart.Utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.locart.R;


public class UpdateEmailActivity extends AppCompatActivity implements ConfirmPasswordDialog.OnConfirmPasswordListener {

    EditText mNewEmail, mConfirmEmail;
    Button mSubmitNewEmail;
    String sEmail, sConfirmEmail;

    /* firebase */
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "UpdateEmailActivity";
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        mNewEmail = findViewById(R.id.newEmail);
        mConfirmEmail = findViewById(R.id.enterEmailAgain);
        mSubmitNewEmail = findViewById(R.id.submitNewEmail);

//        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userId = mAuth.getCurrentUser().getUid();

        mSubmitNewEmail.setOnClickListener(v -> {
            /* hide keyboard layout */
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            assert inputManager != null;
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            if (mNewEmail.getText().length() == 0) {
                mNewEmail.setError("please fill this field");
                return;
            }
            if (checkEmailNotMatch()) {
                ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();
                dialog.show(getSupportFragmentManager(), "ConfirmPasswordDialog");
            } else {
                /* passwords are different, show toast with error message */
                Toast.makeText(getApplicationContext(), "Email does not match", Toast.LENGTH_LONG).show();
            }


        });

    }

    /* check email for match */
    private boolean checkEmailNotMatch() {
        /* get values of edit text to save in string temporarily */
        sEmail = mNewEmail.getText().toString();
        sConfirmEmail = mConfirmEmail.getText().toString();

        /* check for password if matched when entered twice */
        if (!sEmail.equals(sConfirmEmail)) {
            mConfirmEmail.setError(getString(R.string.err_msg_password_confirm));
            mNewEmail.setError(getString(R.string.err_msg_password_confirm));
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /* go back to settings */
        finish();
    }

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: got the password: " + password);

        /* Declare progressDialog before so you can use .hide() later! */
        progressDialog = new ProgressDialog(UpdateEmailActivity.this);
        progressDialog.setMessage("Updating Email...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        /*
         Get auth credentials from the user for re-authentication. The example below shows
         email and password credentials but there are multiple possible providers,
         such as GoogleAuthProvider or FacebookAuthProvider.
        */
        AuthCredential credential = EmailAuthProvider
                .getCredential(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()), password);

        /* Prompt the user to re-provide their sign-in credentials */
        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    /* hide keyboard layout */
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    assert inputManager != null;
                    inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "User re-authenticated.");

                        /* check to see if the email is not already present in the database */
                        mAuth.fetchSignInMethodsForEmail(mNewEmail.getText().toString()).addOnCompleteListener((OnCompleteListener<SignInMethodQueryResult>) task1 -> {
                            if (task1.isSuccessful()) {
                                try {
                                    if (Objects.requireNonNull(task1.getResult()).getSignInMethods().size() == 1) {
                                        Log.d(TAG, "onComplete: that email is already in use.");
                                        progressDialog.hide();
                                        Toast.makeText(UpdateEmailActivity.this, "That email is already in use", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d(TAG, "onComplete: That email is available.");

                                        /* the email is available so update it */
                                        mAuth.getCurrentUser().updateEmail(mNewEmail.getText().toString())
                                                .addOnCompleteListener(task2 -> {
                                                    if (task2.isSuccessful()) {
                                                        Log.d(TAG, "User email address updated.");
//                                                        Toast.makeText(UpdateEmailActivity.this, "email updated", Toast.LENGTH_SHORT).show();

                                                        // change email in fireStore
                                                        changeEmailForCurrentUser();
                                                    }
                                                });
                                    }
                                } catch (NullPointerException e) {
                                    progressDialog.hide();
                                    Log.e(TAG, "onComplete: NullPointerException: " + e.getMessage());
                                    Toast.makeText(UpdateEmailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            }
                        });

                    } else {
                        progressDialog.hide();
                        Log.d(TAG, "onComplete: re-authentication failed.");
                        Toast.makeText(UpdateEmailActivity.this, "Re-authentication failed", Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void changeEmailForCurrentUser() {

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(userId)
                .update(
                        "email", mNewEmail.getText().toString()
                );

        //send verification email to the new email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressDialog.hide();
                        if (task.isSuccessful()) {
                            // email sent
                            // after email is sent just logout the user and finish this activity
                            AlertDialog.Builder dailog = new AlertDialog.Builder(UpdateEmailActivity.this);
                            dailog.setTitle("Email Updated");
                            dailog.setMessage("Check your new email inbox & click on the verify email link to login successfully with your updated email");
                            dailog.setPositiveButton("Dismiss", (dialog, which) -> {
                                FirebaseAuth.getInstance().signOut();
                                /* send user to welcome screen when email has been changed */
                                Intent intent = new Intent(UpdateEmailActivity.this, WelcomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            });

                            AlertDialog alertDialog = dailog.create();
                            alertDialog.setCanceledOnTouchOutside(false);
                            alertDialog.show();

                        } else {
                            progressDialog.hide();
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            Toast.makeText(UpdateEmailActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


    private void OnlineUser() {
        String currentUser = firebaseUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUser)
                .update(arrayOnlineUser);
    }

    private void UserStatus(String status) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            String currentUser = firebaseUser.getUid();

            Map<String, Object> arrayUserStatus = new HashMap<>();
            arrayUserStatus.put("user_status", status);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
                    .document(currentUser)
                    .update(arrayUserStatus);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserStatus("online");
        OnlineUser();

        if (Locart.notificationManagerCompat != null) {
            Locart.notificationManagerCompat.cancelAll();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        UserStatus("paused");
        Locart.appRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UserStatus("offline");
    }

}