package com.example.moodresetwheel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BreathSyncActivity extends AppCompatActivity {

    private View breathingCircle;
    private TextView breathStateText;
    private TextView timerText;
    private ValueAnimator breathAnimator;
    private CountDownTimer countDownTimer;

    private static final int TOTAL_DURATION = 30000; // 30 seconds
    private static final int BREATH_CYCLE = 8000; // 8 seconds per cycle (4s in, 4s out)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_sync);

        breathingCircle = findViewById(R.id.breathingCircle);
        breathStateText = findViewById(R.id.breathStateText);
        timerText = findViewById(R.id.timerText);

        startBreathingExercise();
    }

    private void startBreathingExercise() {
        // Start countdown timer
        countDownTimer = new CountDownTimer(TOTAL_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                timerText.setText(secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                timerText.setText("Complete!");
                breathStateText.setText("Well Done!");
                if (breathAnimator != null) {
                    breathAnimator.cancel();
                }
            }
        };
        countDownTimer.start();

        // Start breathing animation
        startBreathingAnimation();
    }

    private void startBreathingAnimation() {
        // Animate from small (0.5x) to large (1.5x) and back
        breathAnimator = ValueAnimator.ofFloat(0.5f, 1.5f);
        breathAnimator.setDuration(BREATH_CYCLE / 2); // 4 seconds for inhale
        breathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breathAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breathAnimator.setRepeatCount(ValueAnimator.INFINITE);

        breathAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scale = (float) animation.getAnimatedValue();
                breathingCircle.setScaleX(scale);
                breathingCircle.setScaleY(scale);
            }
        });

        breathAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean isInhaling = true;

            @Override
            public void onAnimationRepeat(Animator animation) {
                isInhaling = !isInhaling;
                if (isInhaling) {
                    breathStateText.setText("Breathe In");
                    breathStateText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                } else {
                    breathStateText.setText("Breathe Out");
                    breathStateText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                }
            }
        });

        breathAnimator.start();
        breathStateText.setText("Breathe In");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (breathAnimator != null) {
            breathAnimator.cancel();
        }
    }
}