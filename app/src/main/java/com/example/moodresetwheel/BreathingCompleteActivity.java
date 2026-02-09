package com.example.moodresetwheel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BreathingCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_complete);

        // Mark breathing as completed
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("has_completed_breathing", true).apply();

        MaterialButton btnLetsStart = findViewById(R.id.btnLetsStart);

        // Single button to go to Welcome
        btnLetsStart.setOnClickListener(v -> {
            goToWelcome();
        });
    }

    private void goToWelcome() {
        Intent i = new Intent(this, WelcomeActivity.class);
        startActivity(i);
        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        finish();
    }
}