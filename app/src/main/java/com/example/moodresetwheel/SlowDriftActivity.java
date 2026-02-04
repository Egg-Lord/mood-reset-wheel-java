package com.example.moodresetwheel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class SlowDriftActivity extends AppCompatActivity {

    private SlowDriftView slowDriftView;
    private MediaPlayer player;
    private Handler handler;
    private Runnable timerRunnable;
    private int feelingLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slow_drift);

        // Handle back button press with new API
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Cleanup and go back to wheel
                cleanup();

                Intent i = new Intent(SlowDriftActivity.this, WheelActivity.class);
                i.putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel);
                startActivity(i);
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                finish();
            }
        });

        // Get feeling level from intent
        feelingLevel = getIntent().getIntExtra(
                FeelingLogActivity.KEY_FEELING_LEVEL, 0
        );

        // Initialize views
        slowDriftView = findViewById(R.id.slowDriftView);
        TextView instructionText = findViewById(R.id.instructionText);
        Button doneButton = findViewById(R.id.doneButton);
        Button spinAgainButton = findViewById(R.id.spinAgainButton);
        Button exitButton = findViewById(R.id.exitButton);

        // Hide buttons initially
        doneButton.setVisibility(Button.GONE);
        spinAgainButton.setVisibility(Button.GONE);
        exitButton.setVisibility(Button.GONE);

        // Set breathing instructions
        instructionText.setText("Sync your breathing with the circle\n↑ Inhale  •  ↓ Exhale");

        // Start calm music
        startMusic();

        // Start the slow drift animation
        slowDriftView.startAnimation();

        // Start 60 second timer
        startTimer(doneButton, spinAgainButton, exitButton);

        // Done button click listener
        doneButton.setOnClickListener(v -> {
            // Stop animation and music
            cleanup();

            // Show spin again and exit buttons
            doneButton.setVisibility(Button.GONE);
            spinAgainButton.setVisibility(Button.VISIBLE);
            exitButton.setVisibility(Button.VISIBLE);
        });

        // Spin Again button
        spinAgainButton.setOnClickListener(v -> {
            cleanup();

            Intent i = new Intent(this, WheelActivity.class);
            i.putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel);
            startActivity(i);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
            finish();
        });

        // Exit button
        exitButton.setOnClickListener(v -> {
            cleanup();
            finishAffinity();
        });
    }

    private void startMusic() {
        try {
            player = MediaPlayer.create(this, R.raw.calm_music);
            if (player != null) {
                player.setLooping(true);
                player.setVolume(0.55f, 0.55f);
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer(Button doneButton, Button spinAgainButton, Button exitButton) {
        handler = new Handler(Looper.getMainLooper());
        timerRunnable = () -> {
            // Show done button after 60 seconds
            if (doneButton != null) {
                doneButton.setVisibility(Button.VISIBLE);
                doneButton.setAlpha(0f);
                doneButton.animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start();
            }
        };

        // 60 seconds = 60000 milliseconds
        handler.postDelayed(timerRunnable, 60000);
    }

    private void cleanup() {
        // Stop animation
        if (slowDriftView != null) {
            slowDriftView.stopAnimation();
        }

        // Cancel timer
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }

        // Stop music
        if (player != null) {
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause music when activity is not visible
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music when activity comes back
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }
}