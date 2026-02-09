package com.example.moodresetwheel;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class WheelActivity extends AppCompatActivity {

    private int feelingLevel;
    private boolean spinning = false;
    private float currentRotation = 0f;

    private FrameLayout wheelContainer;
    private TextView statusText;

    private SoundPool soundPool;
    private int tickSoundId;
    private boolean soundsReady;

    private static final String KEY_FEELING_LEVEL =
            FeelingLogActivity.KEY_FEELING_LEVEL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wheel);

        feelingLevel = getIntent().getIntExtra(KEY_FEELING_LEVEL, 0);

        wheelContainer = findViewById(R.id.wheelContainer);
        statusText = findViewById(R.id.statusText);

        initTickSound();

        wheelContainer.setOnClickListener(v -> {
            if (!spinning) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                spinWheel();
            }
        });
    }

    private void initTickSound() {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(attrs)
                .build();

        tickSoundId = soundPool.load(this, R.raw.wheel_tick, 1);
        soundPool.setOnLoadCompleteListener((sp, id, status) -> soundsReady = true);
    }

    // ================= SPIN WITH SNAP =================

    private void spinWheel() {

        spinning = true;
        statusText.setText("Spinning...");

        float extraSpins = 4 * 360f;

        int targetSlice = pickLockedSlice();   // only allowed slices

        float sliceSize = 360f / 8f;           // 45°
        float sliceCenter = targetSlice * sliceSize + sliceSize / 2f;

        float arrowAngle = 270f;               // arrow at top

        float targetRotation =
                currentRotation
                        + extraSpins
                        + (arrowAngle - sliceCenter);

        wheelContainer.animate()
                .rotation(targetRotation)
                .setDuration(2600)
                .setInterpolator(new DecelerateInterpolator(1.4f))
                .setUpdateListener(a -> {
                    currentRotation = wheelContainer.getRotation();
                    if (soundsReady) {
                        soundPool.play(tickSoundId, 0.45f, 0.45f, 1, 0, 1f);
                    }
                })
                .withEndAction(() -> {
                    routeToSlice(targetSlice);
                    spinning = false;
                })
                .start();
    }

    // ================= MOOD LOCK =================

    private int pickLockedSlice() {

        int r = new Random().nextInt(2); // 0 or 1

        switch (feelingLevel) {
            case 0: return r;        // Soft Sort, Ambient
            case 1: return 2 + r;    // Tap Calm, BreathSync
            case 2: return 4 + r;    // Pattern, Box
            case 3: return 6 + r;    // Hold, Drift
        }
        return 0;
    }

    // ================= ROUTING =================

    private void routeToSlice(int slice) {

        Intent intent;

        switch (slice) {

            case 0:
                intent = new Intent(this, SoftSortActivity.class);
                break;

            case 1:
                intent = new Intent(this, AmbientTapLoopActivity.class);
                break;

            case 2:
                intent = new Intent(this, TapTheCalmActivity.class);
                break;

            case 3:
                intent = new Intent(this, BreathSyncActivity.class);
                break;

            case 4:
                intent = new Intent(this, PatternEchoActivity.class);
                break;

            case 5:
                intent = new Intent(this, BoxBreathingActivity.class);
                break;

            case 6:
                intent = new Intent(this, HoldReleaseActivity.class);
                break;

            case 7:
                intent = new Intent(this, SlowDriftActivity.class);
                break;

            default:
                return;
        }

        intent.putExtra(KEY_FEELING_LEVEL, feelingLevel);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) soundPool.release();
    }
}
