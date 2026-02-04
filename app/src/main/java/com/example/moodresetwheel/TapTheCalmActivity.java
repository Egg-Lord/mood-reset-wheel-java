package com.example.moodresetwheel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class TapTheCalmActivity extends AppCompatActivity {

    private int tapCount = 0;
    private final int maxTaps = 10;
    private int feelingLevel = 1;

    private MediaPlayer backgroundPlayer;
    private boolean musicMuted = false;

    private SoundPool soundPool;
    private int tapSoundId = 0;

    private View calmCircle;
    private FrameLayout gameArea;
    private TextView countdownText;
    private TextView tapCounterText;
    private LinearLayout bottomButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_the_calm);

        feelingLevel = getIntent().getIntExtra("FEELING_LEVEL", 1);

        backgroundPlayer = MediaPlayer.create(this, R.raw.game_music);
        backgroundPlayer.setLooping(true);
        backgroundPlayer.setVolume(0.4f, 0.4f);
        backgroundPlayer.start();

        soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        tapSoundId = soundPool.load(this, R.raw.tap_sound, 1);

        ImageButton muteButton = findViewById(R.id.muteButton);
        gameArea = findViewById(R.id.gameArea);
        calmCircle = findViewById(R.id.calmCircle);
        Button startButton = findViewById(R.id.startButton);
        countdownText = findViewById(R.id.countdownText);
        tapCounterText = findViewById(R.id.tapCounterText);
        bottomButtons = findViewById(R.id.bottomButtons);
        Button spinAgainButton = findViewById(R.id.spinAgainButton);
        Button exitButton = findViewById(R.id.exitButton);

        tapCounterText.setText("0 / " + maxTaps);

        muteButton.setOnClickListener(v -> {
            musicMuted = !musicMuted;
            if (musicMuted) {
                backgroundPlayer.pause();
                muteButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
            } else {
                backgroundPlayer.start();
                muteButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
            }
        });

        startButton.setOnClickListener(v -> {
            startButton.setVisibility(View.GONE);
            countdownText.setVisibility(View.VISIBLE);

            new CountDownTimer(3000, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    countdownText.setText(String.valueOf(millisUntilFinished / 1000 + 1));
                }

                @Override
                public void onFinish() {
                    countdownText.setText("GO!");
                    countdownText.animate()
                            .alpha(0f)
                            .setDuration(600)
                            .withEndAction(() -> {
                                countdownText.setVisibility(View.GONE);
                                calmCircle.setVisibility(View.VISIBLE);
                                moveCircleRandomly();
                            })
                            .start();
                }
            }.start();
        });

        calmCircle.setOnClickListener(v -> {
            soundPool.play(tapSoundId, 1f, 1f, 0, 0, 1f);
            tapCount++;

            tapCounterText.setText(tapCount + " / " + maxTaps);

            if (tapCount >= maxTaps) {
                tapCounterText.setText(maxTaps + " / " + maxTaps);
                calmCircle.setVisibility(View.GONE);
                bottomButtons.setVisibility(View.VISIBLE);
            } else {
                moveCircleRandomly();
            }
        });

        spinAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WheelActivity.class);
            intent.putExtra("FEELING_LEVEL", feelingLevel);
            startActivity(intent);
            finish();
        });

        exitButton.setOnClickListener(v -> finishAffinity());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.release();
        }
        soundPool.release();
    }

    private void moveCircleRandomly() {
        int maxX = gameArea.getWidth() - calmCircle.getWidth();
        int maxY = gameArea.getHeight() - calmCircle.getHeight();

        if (maxX <= 0 || maxY <= 0) return;

        Random random = new Random();

        float newX = random.nextInt(maxX);
        float newY = random.nextInt(maxY);

        calmCircle.animate()
                .x(newX)
                .y(newY)
                .setDuration(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
}
