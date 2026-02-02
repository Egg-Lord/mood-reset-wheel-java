package com.example.moodresetwheel;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class WheelActivity extends AppCompatActivity {

    private int feelingLevel = 0;
    private boolean spinning = false;
    private float currentRotation = 0f;

    private FrameLayout wheelContainer;
    private TextView labelTop;
    private TextView labelBottom;
    private TextView statusText;

    // Tick sound
    private SoundPool soundPool;
    private int tickSoundId = 0;
    private boolean soundsReady = false;

    // Pulse animators
    private ObjectAnimator pulseTop;
    private ObjectAnimator pulseBottom;

    private static final String KEY_FEELING_LEVEL =
            FeelingLogActivity.KEY_FEELING_LEVEL;
    private static final String KEY_ACTIVITY_ID = "ACTIVITY_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        if (!getIntent().hasExtra(KEY_FEELING_LEVEL)) {
            finish();
            return;
        }

        feelingLevel = getIntent().getIntExtra(KEY_FEELING_LEVEL, 0);

        wheelContainer = findViewById(R.id.wheelContainer);
        labelTop = findViewById(R.id.labelTop);
        labelBottom = findViewById(R.id.labelBottom);

        try {
            statusText = findViewById(R.id.statusText);
        } catch (Exception e) {
            statusText = null;
        }

        initTickSound();
        applyLabelsForFeeling(feelingLevel);
        startLabelPulse();

        if (statusText != null) {
            statusText.setText("Tap the wheel to spin");
        }

        wheelContainer.setOnClickListener(v -> {
            if (!spinning) {
                v.performHapticFeedback(
                        HapticFeedbackConstants.KEYBOARD_TAP
                );
                spinWheel();
            }
        });
    }

    private void applyLabelsForFeeling(int level) {
        switch (level) {
            case 0:
                labelTop.setText("Soft Sort");
                labelBottom.setText("Ambient Tap Loop");
                break;
            case 1:
                labelTop.setText("Breathing Exercise");
                labelBottom.setText("Breath Sync");
                break;
            case 2:
                labelTop.setText("Pattern Echo");
                labelBottom.setText("Box Breathing");
                break;
            case 3:
                labelTop.setText("Hold & Release");
                labelBottom.setText("Slow Drift");
                break;
            default:
                labelTop.setText("Breathing Exercise");
                labelBottom.setText("Breath Sync");
        }
    }

    private void startLabelPulse() {
        pulseTop = makePulse(labelTop);
        pulseBottom = makePulse(labelBottom);
        pulseTop.start();
        pulseBottom.start();
    }

    private ObjectAnimator makePulse(View view) {
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(
                        View.SCALE_X, 1f, 1.10f
                ),
                PropertyValuesHolder.ofFloat(
                        View.SCALE_Y, 1f, 1.10f
                )
        );
        anim.setDuration(650);
        anim.setRepeatMode(ObjectAnimator.REVERSE);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.setInterpolator(new LinearInterpolator());
        return anim;
    }

    private void stopLabelPulse() {
        if (pulseTop != null) pulseTop.cancel();
        if (pulseBottom != null) pulseBottom.cancel();

        labelTop.setScaleX(1f);
        labelTop.setScaleY(1f);
        labelBottom.setScaleX(1f);
        labelBottom.setScaleY(1f);
    }

    private void initTickSound() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                )
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build();

        tickSoundId = soundPool.load(
                this, R.raw.wheel_tick, 1
        );

        soundPool.setOnLoadCompleteListener(
                (sp, id, status) -> soundsReady = true
        );
    }

    private void spinWheel() {
        spinning = true;
        stopLabelPulse();

        if (statusText != null) {
            statusText.setText("Spinning…");
        }

        float start = currentRotation;
        float extraSpins = 4 * 360f;
        float offset = new Random().nextInt(360);
        float end = start + extraSpins + offset;

        final int[] lastTickStep = { (int) start };

        wheelContainer.animate()
                .rotation(end)
                .setDuration(2600)
                .setInterpolator(
                        new DecelerateInterpolator(1.5f)
                )
                .setUpdateListener(animation -> {
                    currentRotation =
                            wheelContainer.getRotation();
                    int step = (int) currentRotation;

                    if (Math.abs(step - lastTickStep[0]) >= 18) {
                        lastTickStep[0] = step;
                        if (soundsReady) {
                            soundPool.play(
                                    tickSoundId,
                                    0.9f,
                                    0.9f,
                                    1,
                                    0,
                                    1f
                            );
                        }
                    }
                })
                .withEndAction(() -> {
                    currentRotation =
                            wheelContainer.getRotation();

                    boolean topSelected =
                            isTopSelected(currentRotation);

                    String chosenName =
                            topSelected
                                    ? labelTop.getText().toString()
                                    : labelBottom.getText().toString();

                    if (statusText != null) {
                        statusText.setText(
                                "Selected: " + chosenName
                        );
                    }

                    routeToActivity(topSelected);
                    spinning = false;
                })
                .start();
    }

    private boolean isTopSelected(float rotation) {
        float normalized =
                ((rotation % 360) + 360) % 360;
        return normalized < 180f;
    }

    private void routeToActivity(boolean topSelected) {

        String activityId;
        switch (feelingLevel) {
            case 0:
                activityId = topSelected
                        ? "SOFT_SORT"
                        : "AMBIENT_TAP_LOOP";
                break;
            case 1:
                activityId = topSelected
                        ? "BREATHING_EXERCISE"
                        : "BREATH_SYNC";
                break;
            case 2:
                activityId = topSelected
                        ? "PATTERN_ECHO"
                        : "BOX_BREATHING";
                break;
            case 3:
                activityId = topSelected
                        ? "HOLD_AND_RELEASE"
                        : "SLOW_DRIFT";
                break;
            default:
                activityId = "BREATHING_EXERCISE";
        }

        android.content.Intent nextIntent;

        if (activityId.equals("BREATHING_EXERCISE")
                || activityId.equals("BREATH_SYNC")
                || activityId.equals("BOX_BREATHING")
                || activityId.equals("HOLD_AND_RELEASE")) {

            nextIntent =
                    new android.content.Intent(
                            this,
                            BreathingActivity.class
                    );
        } else {
            nextIntent =
                    new android.content.Intent(
                            this,
                            PlaceholderActivity.class
                    );
        }

        nextIntent.putExtra(
                KEY_FEELING_LEVEL, feelingLevel
        );
        nextIntent.putExtra(
                KEY_ACTIVITY_ID, activityId
        );

        startActivity(nextIntent);
        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
