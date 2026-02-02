package com.example.moodresetwheel;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlaceholderActivity extends AppCompatActivity {

    private MediaPlayer player = null;
    private int feelingLevel = 0;
    private String activityId = "UNKNOWN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_mini);

        feelingLevel = getIntent().getIntExtra(
                FeelingLogActivity.KEY_FEELING_LEVEL, 0
        );
        String idExtra = getIntent().getStringExtra("ACTIVITY_ID");
        activityId = idExtra != null ? idExtra : "UNKNOWN";

        TextView title = findViewById(R.id.activityTitle);
        FrameLayout preview = findViewById(R.id.previewArea);
        Button startButton = findViewById(R.id.startButton);
        Button spinAgainButton = findViewById(R.id.spinAgainButton);
        Button exitButton = findViewById(R.id.exitButton);

        title.setText(prettyName(activityId));

        preview.removeAllViews();
        TextView msg = new TextView(this);
        msg.setText(
                "This activity is coming soon.\n\n" +
                        "Try the Breathing Exercise for now 🌿"
        );
        msg.setTextColor(0xFF2B2A28);
        msg.setTextSize(16f);
        msg.setGravity(Gravity.CENTER);
        preview.addView(msg);

        spinAgainButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.GONE);

        startButton.setOnClickListener(v -> {

            // Route breathing-related activities
            if (activityId.equals("BREATHING_EXERCISE")
                    || activityId.equals("BREATH_SYNC")
                    || activityId.equals("BOX_BREATHING")
                    || activityId.equals("HOLD_AND_RELEASE")) {

                Intent i = new Intent(this, BreathingActivity.class);
                i.putExtra(
                        FeelingLogActivity.KEY_FEELING_LEVEL,
                        feelingLevel
                );
                i.putExtra("ACTIVITY_ID", activityId);

                startActivity(i);
                overridePendingTransition(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                );
                finish();
                return;
            }

            startButton.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(90)
                    .withEndAction(() ->
                            startButton.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(140)
                                    .setInterpolator(
                                            new OvershootInterpolator()
                                    )
                                    .start()
                    )
                    .start();

            if (player != null) {
                player.release();
            }

            player = MediaPlayer.create(this, R.raw.calm_music);
            player.setLooping(true);
            player.setVolume(0.55f, 0.55f);
            player.start();

            startButton.setVisibility(View.GONE);
            spinAgainButton.setVisibility(View.VISIBLE);
            exitButton.setVisibility(View.VISIBLE);

            msg.setText(
                    "This activity is coming soon.\n\n" +
                            "Try the Breathing Exercise for now 🌿"
            );
        });

        spinAgainButton.setOnClickListener(v -> {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }

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

        exitButton.setOnClickListener(v -> finishAffinity());
    }

    private String prettyName(String id) {
        switch (id) {
            case "SOFT_SORT":
                return "Soft Sort";
            case "AMBIENT_TAP_LOOP":
                return "Ambient Tap Loop";
            case "BREATHING_EXERCISE":
                return "Breathing Exercise";
            case "BREATH_SYNC":
                return "Breath Sync";
            case "PATTERN_ECHO":
                return "Pattern Echo";
            case "BOX_BREATHING":
                return "Box Breathing";
            case "HOLD_AND_RELEASE":
                return "Hold & Release";
            case "SLOW_DRIFT":
                return "Slow Drift";
            default:
                return "Mini Activity";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
