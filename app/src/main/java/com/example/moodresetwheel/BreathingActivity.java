package com.example.moodresetwheel;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class BreathingActivity extends AppCompatActivity {

    private View breathCircle;
    private TextView tvPhase;
    private TextView tvTimer;
    private MaterialButton btnCenter;
    private MaterialButton btnSlow;
    private MaterialButton btnNormal;
    private MaterialButton btnFast;

    private boolean isRunning = false;
    private CountDownTimer phaseTimer;
    private AnimatorSet animatorSet;

    private int cycleCount = 0;
    private static final int MAX_CYCLES = 3;

    private enum SpeedMode { SLOW, NORMAL, FAST }
    private SpeedMode speedMode = SpeedMode.NORMAL;

    private static class Pattern {
        int inhale, hold1, exhale, hold2;
        Pattern(int i, int h1, int e, int h2) {
            inhale = i; hold1 = h1; exhale = e; hold2 = h2;
        }
    }

    private Pattern patternFor(SpeedMode mode) {
        switch (mode) {
            case SLOW:
                return new Pattern(5, 2, 6, 2);
            case FAST:
                return new Pattern(3, 1, 3, 1);
            case NORMAL:
            default:
                return new Pattern(4, 2, 4, 2);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breathing);

        breathCircle = findViewById(R.id.breathCircle);
        tvPhase = findViewById(R.id.tvPhase);
        tvTimer = findViewById(R.id.tvTimer);
        btnCenter = findViewById(R.id.btnBreathStart);
        btnSlow = findViewById(R.id.btnSlow);
        btnNormal = findViewById(R.id.btnNormal);
        btnFast = findViewById(R.id.btnFast);

        highlightButton(btnNormal);
        setReadyUI();

        setSpeed(SpeedMode.NORMAL);
        setReadyUI();

        btnCenter.setOnClickListener(v -> {
            if (isRunning) {
                stopBreathing(true);
            } else {
                startBreathing();
            }
        });

        btnSlow.setOnClickListener(v -> { if (!isRunning) setSpeed(SpeedMode.SLOW); });
        btnNormal.setOnClickListener(v -> { if (!isRunning) setSpeed(SpeedMode.NORMAL); });
        btnFast.setOnClickListener(v -> { if (!isRunning) setSpeed(SpeedMode.FAST); });
    }

    private void setSpeed(SpeedMode mode) {
        speedMode = mode;

        // Reset ALL buttons to secondary/outlined style
        resetButtonToSecondary(btnSlow);
        resetButtonToSecondary(btnNormal);
        resetButtonToSecondary(btnFast);

        // Highlight the selected button
        switch (mode) {
            case SLOW:
                highlightButton(btnSlow);
                break;
            case NORMAL:
                highlightButton(btnNormal);
                break;
            case FAST:
                highlightButton(btnFast);
                break;
        }

        // Update the enabled state
        btnSlow.setEnabled(mode != SpeedMode.SLOW);
        btnNormal.setEnabled(mode != SpeedMode.NORMAL);
        btnFast.setEnabled(mode != SpeedMode.FAST);

        if (!isRunning) {
            switch (mode) {
                case SLOW:
                    tvTimer.setText(getString(R.string.breathing_speed_slow));
                    break;
                case FAST:
                    tvTimer.setText(getString(R.string.breathing_speed_fast));
                    break;
                case NORMAL:
                default:
                    tvTimer.setText(getString(R.string.breathing_speed_normal));
            }
        }
    }

    private void highlightButton(MaterialButton button) {
        // Make button filled with green background and white text
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.brand_500)));
        button.setTextColor(getResources().getColor(R.color.white));
        button.setStrokeWidth(0); // Remove outline
    }

    private void resetButtonToSecondary(MaterialButton button) {
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(android.R.color.transparent)));
        button.setTextColor(getResources().getColor(R.color.text_primary));
        button.setStrokeColor(getResources().getColorStateList(R.color.outline));
        button.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.stroke_width));
    }

    private void setReadyUI() {
        tvPhase.setText(getString(R.string.breathing_ready));
        tvTimer.setText(getString(R.string.breathing_tap_to_start));

        btnCenter.setText(getString(R.string.start));
        btnCenter.setIconResource(android.R.drawable.ic_media_play);

        breathCircle.setScaleX(0.78f);
        breathCircle.setScaleY(0.78f);
        breathCircle.setAlpha(1f);
    }

    private void startBreathing() {
        isRunning = true;
        cycleCount = 0;

        btnCenter.setText(getString(R.string.stop));
        btnCenter.setIconResource(android.R.drawable.ic_media_pause);

        btnSlow.setEnabled(false);
        btnNormal.setEnabled(false);
        btnFast.setEnabled(false);

        runCycle();
    }

    private void runCycle() {
        if (!isRunning) return;

        Pattern p = patternFor(speedMode);

        runPhase(getString(R.string.breathing_inhale), p.inhale,
                () -> animateCircle(1.06f, p.inhale * 1000L),
                () -> runPhase(getString(R.string.breathing_hold), p.hold1,
                        () -> {}, () -> runPhase(getString(R.string.breathing_exhale), p.exhale,
                                () -> animateCircle(0.78f, p.exhale * 1000L),
                                () -> runPhase(getString(R.string.breathing_hold), p.hold2,
                                        () -> {}, () -> {
                                            cycleCount++;
                                            if (cycleCount >= MAX_CYCLES) {
                                                stopBreathing(true);
                                            } else {
                                                runCycle();
                                            }
                                        }))));
    }

    private void runPhase(String label, int seconds, Runnable animate, Runnable onDone) {
        if (!isRunning) return;

        tvPhase.setText(label);
        animate.run();

        if (phaseTimer != null) phaseTimer.cancel();
        phaseTimer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int remaining = (int) (millisUntilFinished / 1000L) + 1;
                tvTimer.setText(getString(R.string.breathing_seconds, remaining));
            }

            @Override
            public void onFinish() {
                tvTimer.setText(getString(R.string.breathing_seconds, 0));
                onDone.run();
            }
        }.start();
    }

    private void animateCircle(float toScale, long durationMs) {
        if (animatorSet != null) animatorSet.cancel();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(breathCircle, View.SCALE_X, breathCircle.getScaleX(), toScale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(breathCircle, View.SCALE_Y, breathCircle.getScaleY(), toScale);

        animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(durationMs);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void stopBreathing(boolean goToComplete) {
        isRunning = false;

        if (phaseTimer != null) phaseTimer.cancel();
        phaseTimer = null;

        if (animatorSet != null) animatorSet.cancel();
        animatorSet = null;

        setSpeed(speedMode);

        if (goToComplete) {
            goToCompleteScreen();
        } else {
            setReadyUI();
        }
    }

    private void goToCompleteScreen() {
        Intent i = new Intent(this, BreathingCompleteActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isRunning) stopBreathing(false);
    }
}