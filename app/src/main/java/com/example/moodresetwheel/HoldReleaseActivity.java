package com.example.moodresetwheel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.animation.ObjectAnimator;
import android.view.animation.LinearInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class HoldReleaseActivity extends AppCompatActivity {

    private ProgressBar holdProgressBar;
    private TextView holdInstructionText;
    private TextView holdRoundCounter;
    private View holdCircleView;
    private Button holdStartButton;

    private int currentRound = 0;
    private final int TOTAL_ROUNDS = 3;
    private final long HOLD_DURATION_MS = 5000;

    private boolean isHolding = false;
    private boolean holdCompleted = false; // NEW: Track if hold was completed

    private ObjectAnimator progressAnim;
    private android.os.Handler handler = new android.os.Handler();
    private Runnable finishHold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hold_release);

        holdProgressBar = findViewById(R.id.holdProgressBar);
        holdInstructionText = findViewById(R.id.holdInstructionText);
        holdRoundCounter = findViewById(R.id.holdRoundCounter);
        holdCircleView = findViewById(R.id.holdCircleView);
        holdStartButton = findViewById(R.id.holdStartButton);

        Button spinAgain = findViewById(R.id.holdSpinAgainButton);
        Button exit = findViewById(R.id.holdExitButton);

        resetUI();

        holdStartButton.setOnClickListener(v -> {
            startRound();
            setupHoldListener();
        });

        spinAgain.setOnClickListener(v -> {
            startActivity(new Intent(this, WheelActivity.class));
            finish();
        });

        exit.setOnClickListener(v -> finishAffinity());
    }

    private void setupHoldListener() {
        holdCircleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isHolding) return false;

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    holdCompleted = false; // Reset flag when starting new hold

                    holdCircleView.setBackground(
                            ContextCompat.getDrawable(
                                    HoldReleaseActivity.this,
                                    R.drawable.hold_circle_holding
                            )
                    );

                    progressAnim = ObjectAnimator.ofInt(
                            holdProgressBar, "progress", 0, 100
                    );
                    progressAnim.setDuration(HOLD_DURATION_MS);
                    progressAnim.setInterpolator(new LinearInterpolator());
                    progressAnim.start();

                    finishHold = () -> {
                        holdCompleted = true; // Mark as completed
                        completeRound();
                    };

                    handler.postDelayed(finishHold, HOLD_DURATION_MS);
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Only treat as early release if the hold wasn't completed
                    if (!holdCompleted) {
                        if (finishHold != null) {
                            handler.removeCallbacks(finishHold);
                        }
                        if (progressAnim != null && progressAnim.isRunning()) {
                            progressAnim.cancel();
                        }
                        holdProgressBar.setProgress(0);
                        holdInstructionText.setText("Released too early. Try again.");
                        holdCircleView.setBackground(
                                ContextCompat.getDrawable(
                                        HoldReleaseActivity.this,
                                        R.drawable.hold_circle_normal
                                )
                        );
                    } else {
                        // Hold was completed, ACTION_UP is just finger release after completion
                        // Do nothing or reset the circle background
                        holdCircleView.setBackground(
                                ContextCompat.getDrawable(
                                        HoldReleaseActivity.this,
                                        R.drawable.hold_circle_normal
                                )
                        );
                    }
                    return true;
                }

                return false;
            }
        });
    }

    private void startRound() {
        isHolding = true;
        holdCompleted = false;
        holdStartButton.setVisibility(View.GONE);
        holdInstructionText.setText("Hold for 5 seconds");
        holdRoundCounter.setText("Round: " + (currentRound + 1) + "/" + TOTAL_ROUNDS);
    }

    private void completeRound() {
        // Stop any running animation
        if (progressAnim != null && progressAnim.isRunning()) {
            progressAnim.cancel();
        }

        // Remove any pending callbacks
        if (finishHold != null) {
            handler.removeCallbacks(finishHold);
        }

        currentRound++;

        holdProgressBar.setProgress(100); // Set to full for visual feedback
        holdCircleView.setBackground(
                ContextCompat.getDrawable(this, R.drawable.hold_circle_normal)
        );

        if (currentRound >= TOTAL_ROUNDS) {
            holdInstructionText.setText("Great job! Completed!");
            findViewById(R.id.holdSpinAgainButton).setVisibility(View.VISIBLE);
            findViewById(R.id.holdExitButton).setVisibility(View.VISIBLE);
            isHolding = false;
        } else {
            holdInstructionText.setText("Nice! Next round...");
            new android.os.Handler().postDelayed(() -> {
                holdProgressBar.setProgress(0);
                holdCompleted = false;
                startRound();
            }, 1200);
        }
    }

    private void resetUI() {
        currentRound = 0;
        isHolding = false;
        holdCompleted = false;
        holdProgressBar.setProgress(0);
        holdInstructionText.setText("Hold & Release Activity");
        holdRoundCounter.setText("Press Start");
        holdStartButton.setVisibility(View.VISIBLE);

        findViewById(R.id.holdSpinAgainButton).setVisibility(View.GONE);
        findViewById(R.id.holdExitButton).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}