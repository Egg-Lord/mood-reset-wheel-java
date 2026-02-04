package com.example.moodresetwheel;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BoxBreathingActivity extends AppCompatActivity {

    private Button startButton;
    private TextView breathingText, timerText;
    private View circle;

    private int cycleCount = 0;
    private static final int MAX_CYCLES = 3;
    private static final int PHASE_TIME = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_breathing);

        circle = findViewById(R.id.circle);
        startButton = findViewById(R.id.startButton);
        breathingText = findViewById(R.id.breathingText);
        timerText = findViewById(R.id.timerText);

        startButton.setOnClickListener(v -> {
            startButton.setEnabled(false);
            cycleCount = 0;
            startInhale();
        });
    }

    private void animateCircle(float scale, long duration) {
        ObjectAnimator.ofFloat(circle, View.SCALE_X, scale).setDuration(duration).start();
        ObjectAnimator.ofFloat(circle, View.SCALE_Y, scale).setDuration(duration).start();
    }

    private void startInhale() {
        breathingText.setText("Inhale");
        animateCircle(1.5f, PHASE_TIME);
        startTimer(this::startHold1);
    }

    private void startHold1() {
        breathingText.setText("Hold");
        startTimer(this::startExhale);
    }

    private void startExhale() {
        breathingText.setText("Exhale");
        animateCircle(1f, PHASE_TIME);
        startTimer(this::startHold2);
    }

    private void startHold2() {
        breathingText.setText("Hold");
        startTimer(() -> {
            cycleCount++;
            if (cycleCount < MAX_CYCLES) {
                startInhale();
            } else {
                breathingText.setText("Done!");
                timerText.setText("0");
                startButton.setEnabled(true);
            }
        });
    }

    private void startTimer(Runnable onFinish) {
        new CountDownTimer(PHASE_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(String.valueOf(millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                onFinish.run();
            }
        }.start();
    }
}
