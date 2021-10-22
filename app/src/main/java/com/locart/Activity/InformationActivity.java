package com.locart.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.locart.Locart;
import com.locart.R;
import com.locart.Utils.Constants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class InformationActivity extends AppCompatActivity {
    TextView tvInformation;
    private FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUser = firebaseUser.getUid();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        tvInformation = findViewById(R.id.information);
        tvInformation.setText(Html.fromHtml(getString(R.string.information_read)));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void OnlineUser() {
        String currentUserId = firebaseUser.getUid();

        Map<String, Object> arrayOnlineUser = new HashMap<>();
        arrayOnlineUser.put("user_online", Timestamp.now());

        firebaseFirestore.collection(Constants.FIRE_STORE_COLLECTION)
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