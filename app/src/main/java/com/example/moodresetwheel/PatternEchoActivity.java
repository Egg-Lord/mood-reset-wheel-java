package com.example.moodresetwheel;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import java.util.List;
import java.util.Random;

public class PatternEchoActivity extends AppCompatActivity {

    private TextView tvMessage;
    private Button btnCircle, btnSquare, btnTriangle;

    private List<String> pattern = new ArrayList<>();
    private List<String> userInput = new ArrayList<>();
    private String[] shapes = {"Circle", "Square", "Triangle"};
    private int currentStep = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_echo);

        tvMessage = findViewById(R.id.tvMessage);
        btnCircle = findViewById(R.id.btnCircle);
        btnSquare = findViewById(R.id.btnSquare);
        btnTriangle = findViewById(R.id.btnTriangle);


        setButtonsEnabled(false);


        generatePattern();


        showPattern();


        btnCircle.setOnClickListener(v -> handleUserInput("Circle"));
        btnSquare.setOnClickListener(v -> handleUserInput("Square"));
        btnTriangle.setOnClickListener(v -> handleUserInput("Triangle"));
    }

    private void generatePattern() {
        pattern.clear();
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            pattern.add(shapes[random.nextInt(shapes.length)]);
        }
    }

    private void showPattern() {
        tvMessage.setText("Watch the pattern!");
        setButtonsEnabled(false);
        currentStep = 0;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentStep < pattern.size()) {
                    highlightButton(pattern.get(currentStep));
                    currentStep++;
                    handler.postDelayed(this, 1000);
                } else {

                    tvMessage.setText("Your turn!");
                    setButtonsEnabled(true);
                    userInput.clear();
                }
            }
        }, 1000);
    }

    private void highlightButton(String shape) {
        Button btn = getButtonByShape(shape);
        if (btn != null) {
            btn.setAlpha(0.3f); // dim to indicate highlight
            handler.postDelayed(() -> btn.setAlpha(1f), 500); // return to normal after 0.5s
        }
    }

    private Button getButtonByShape(String shape) {
        switch (shape) {
            case "Circle": return btnCircle;
            case "Square": return btnSquare;
            case "Triangle": return btnTriangle;
            default: return null;
        }
    }

    private void handleUserInput(String shape) {
        userInput.add(shape);
        int step = userInput.size() - 1;

        // Check current input immediately
        if (!userInput.get(step).equals(pattern.get(step))) {
            Toast.makeText(this, "Wrong! Game over.", Toast.LENGTH_SHORT).show();
            setButtonsEnabled(false);
            tvMessage.setText("Game over!");
            // Return to start screen after a delay
            handler.postDelayed(this::finish, 2000);
            return;
        }

        // If user completes the sequence correctly
        if (userInput.size() == pattern.size()) {
            Toast.makeText(this, "Correct! Well done!", Toast.LENGTH_SHORT).show();
            setButtonsEnabled(false);
            tvMessage.setText("You completed the pattern!");
            // Return to start screen after a delay
            handler.postDelayed(this::finish, 2000);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnCircle.setEnabled(enabled);
        btnSquare.setEnabled(enabled);
        btnTriangle.setEnabled(enabled);
    }
}