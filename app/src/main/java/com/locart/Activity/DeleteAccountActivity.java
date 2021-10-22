package com.locart.Activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.locart.Locart;
import com.locart.Login.WelcomeActivity;
import com.locart.Utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.locart.R;

public class DeleteAccountActivity extends AppCompatActivity {

    Button mDeleteButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        initUi();
    }

    private void initUi() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mDeleteButton = findViewById(R.id.deleteAccountPermanently);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        mDeleteButton.setOnClickListener(v -> {
            AlertDialog.Builder dailog = new AlertDialog.Builder(DeleteAccountActivity.this);
            dailog.setTitle("Are you sure?");
            dailog.setMessage("Multiple deletions of this account may lead to permanent ban on this email and devices associated with this account. Confirm account deletion.");
            dailog.setPositiveButton("Delete account permanently", (dialog, which) ->

                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            /* delete all user data in database and fire store using installed extension */
//                            deleteAllData();
                            Toast.makeText(DeleteAccountActivity.this, "Account Deleted Permanently", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(DeleteAccountActivity.this, WelcomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                        } else {
                            Toast.makeText(DeleteAccountActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }));
            dailog.setNegativeButton("Dismiss", (dialogInterface, i) ->
                    dialogInterface.dismiss());
            AlertDialog alertDialog = dailog.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /* go back to settings */
        finish();
    }

    private void OnlineUser() {
        String currentUserId = currentUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        db.collection(Constants.FIRE_STORE_COLLECTION)
                .document(currentUserId)
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