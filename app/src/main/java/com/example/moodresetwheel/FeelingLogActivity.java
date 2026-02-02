package com.example.moodresetwheel;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.animation.OvershootInterpolator;
import android.widget.SeekBar;
import android.content.SharedPreferences;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class FeelingLogActivity extends AppCompatActivity {

    public static final String KEY_FEELING_LEVEL = "FEELING_LEVEL";

    // 0 = Neutral
    // 1 = Tense
    // 2 = Stressed
    // 3 = Overwhelmed
    private int selectedFeeling = 0;

    private MaterialCardView cardNeutral;
    private MaterialCardView cardTense;
    private MaterialCardView cardStressed;
    private MaterialCardView cardOverwhelmed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeling_log);

        TextView feelingTitle = findViewById(R.id.feelingTitle);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");

        if (username != null && !username.trim().isEmpty()) {
            feelingTitle.setText("How are ya feeling, " + username + "?");
        }


        SeekBar feelingSeekBar = findViewById(R.id.feelingSeekBar);
        MaterialButton continueButton = findViewById(R.id.continueButton);

        cardNeutral = findViewById(R.id.cardNeutral);
        cardTense = findViewById(R.id.cardTense);
        cardStressed = findViewById(R.id.cardStressed);
        cardOverwhelmed = findViewById(R.id.cardOverwhelmed);

        // 4 discrete points
        feelingSeekBar.setMax(3);
        feelingSeekBar.setProgress(0);

        // Default highlight
        animateSelection(0);

        feelingSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser
            ) {
                selectedFeeling = Math.max(0, Math.min(progress, 3));
                animateSelection(selectedFeeling);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // snap to nearest point
                int snapped = Math.round(seekBar.getProgress());
                seekBar.setProgress(snapped, true);
            }
        });

        continueButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            Intent i = new Intent(this, WheelActivity.class);
            i.putExtra(KEY_FEELING_LEVEL, selectedFeeling);

            startActivity(i);
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
            );
        });
    }

    // 🔥 Animate + outline the selected card
    private void animateSelection(int mood) {
        resetCard(cardNeutral);
        resetCard(cardTense);
        resetCard(cardStressed);
        resetCard(cardOverwhelmed);

        MaterialCardView target;
        int strokeColor;

        switch (mood) {
            case 1:
                target = cardTense;
                strokeColor = 0xFFFB923C; // amber
                break;
            case 2:
                target = cardStressed;
                strokeColor = 0xFFF97316; // orange
                break;
            case 3:
                target = cardOverwhelmed;
                strokeColor = 0xFFEF4444; // red
                break;
            case 0:
            default:
                target = cardNeutral;
                strokeColor = 0xFF2DD4BF; // teal
                break;
        }

        target.setStrokeWidth(4);
        target.setStrokeColor(strokeColor);

        target.animate()
                .scaleX(1.12f)
                .scaleY(1.12f)
                .setDuration(180)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() ->
                        target.animate()
                                .scaleX(1.05f)
                                .scaleY(1.05f)
                                .setDuration(120)
                                .start()
                )
                .start();
    }

    private void resetCard(MaterialCardView card) {
        card.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(120)
                .start();

        card.setStrokeWidth(2);
        card.setStrokeColor(0xFFE5E7EB); // default gray
    }
}
