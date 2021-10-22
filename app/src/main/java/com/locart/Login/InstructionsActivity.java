package com.locart.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.locart.R;

public class InstructionsActivity extends AppCompatActivity {

    Button mProceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        mProceed = findViewById(R.id.proceed);

        mProceed.setOnClickListener(v -> {
            Intent intent = new Intent(InstructionsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(InstructionsActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}