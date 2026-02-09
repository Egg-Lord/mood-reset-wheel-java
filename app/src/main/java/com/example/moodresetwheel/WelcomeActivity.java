package com.example.moodresetwheel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String savedName = prefs.getString("username", null);

        if (savedName != null && !savedName.trim().isEmpty()) {
            goToFeelingLog();
            return;
        }

        // Use the actual welcome layout
        setContentView(R.layout.activity_welcome);

        TextInputEditText etName = findViewById(R.id.etUsername);
        MaterialButton btnContinue = findViewById(R.id.btnContinue);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

                String name = etName.getText() != null
                        ? etName.getText().toString().trim()
                        : "";

                if (name.isEmpty()) {
                    Toast.makeText(
                            WelcomeActivity.this,
                            "Please enter your username.",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                prefs.edit().putString("username", name).apply();
                goToFeelingLog();
            }
        });
    }

    private void goToFeelingLog() {
        startActivity(new Intent(this, FeelingLogActivity.class));
        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );
        finish();
    }
}