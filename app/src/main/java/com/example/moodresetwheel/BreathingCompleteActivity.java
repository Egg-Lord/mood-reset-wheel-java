package com.example.moodresetwheel;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BreathingCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing_complete);

        int feelingLevel = getIntent().getIntExtra(
                FeelingLogActivity.KEY_FEELING_LEVEL, 0
        );

        String activityId = getIntent().getStringExtra("ACTIVITY_ID");
        if (activityId == null) activityId = "UNKNOWN";

        MaterialButton btnTryAnotherTask =
                findViewById(R.id.btnTryAnotherTask);

        MaterialButton btnSpinAgain =
                findViewById(R.id.btnSpinAgain);

        // Try another task = go back to wheel
        btnTryAnotherTask.setOnClickListener(v -> {
            Intent i = new Intent(this, WheelActivity.class);
            i.putExtra(
                    FeelingLogActivity.KEY_FEELING_LEVEL,
                    feelingLevel
            );
            startActivity(i);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            finish();
        });

        // Spin again = also back to wheel
        btnSpinAgain.setOnClickListener(v -> {
            Intent i = new Intent(this, WheelActivity.class);
            i.putExtra(
                    FeelingLogActivity.KEY_FEELING_LEVEL,
                    feelingLevel
            );
            startActivity(i);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            finish();
        });
    }
}
