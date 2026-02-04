package com.example.moodresetwheel;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AmbientTapLoopActivity extends AppCompatActivity {

    private RippleFieldView rippleField;
    private View resultPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambient_tap_loop);

        rippleField = findViewById(R.id.rippleField);
        resultPanel = findViewById(R.id.resultPanel);

        rippleField.setListener(() -> {
            resultPanel.setVisibility(View.VISIBLE);
        });

        rippleField.post(() -> rippleField.startNewGame(18));

        findViewById(R.id.btnAgain).setOnClickListener(v -> {
            resultPanel.setVisibility(View.GONE);
            rippleField.startNewGame(18);
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> finish());

        findViewById(R.id.btnBackToLog).setOnClickListener(v -> finish());
    }
}
