package com.example.moodresetwheel;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SlowDriftView extends View {

    private Paint circlePaint;
    private Paint wavePaint;
    private Path wavePath;

    private float circleX;
    private float circleY;
    private final float circleRadius = 40f; // Made final

    private float waveAmplitude;
    private final float waveFrequency = 0.02f; // Made final
    private float phase = 0f;

    private ValueAnimator animator;

    // Color constants for warm theme
    private final int circleColor = Color.parseColor("#2A9D8F"); // brand_500 - sage/teal
    private final int waveColor = Color.parseColor("#9AD9D0");   // brand_200 - light teal
    private final int highlightColor = Color.parseColor("#F4A261"); // accent_500 - warm honey

    public SlowDriftView(@NonNull Context context) { // Added @NonNull
        super(context);
        init();
    }

    public SlowDriftView(@NonNull Context context, @Nullable AttributeSet attrs) { // Added annotations
        super(context, attrs);
        init();
    }

    private void init() {
        // Circle paint (sage/teal)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(circleColor);
        circlePaint.setStyle(Paint.Style.FILL);

        // Add subtle shadow for depth
        circlePaint.setShadowLayer(8f, 2f, 4f, Color.parseColor("#40000000"));

        // Wave paint (light teal)
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(waveColor);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(4f);
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);

        wavePath = new Path();

        // Enable hardware acceleration for shadows
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        waveAmplitude = h * 0.15f;
        circleX = w / 2f;     // center horizontally
        circleY = h / 2f;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) { // Added @NonNull
        super.onDraw(canvas);

        if (getWidth() == 0 || getHeight() == 0) return;

        drawSineWave(canvas);

        float centerY = getHeight() / 2f;
        circleY = centerY + waveAmplitude
                * (float) Math.sin(circleX * waveFrequency + phase);

        // Draw the main circle
        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint);

        // Add a subtle highlight on the circle
        Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(highlightColor);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(3f);
        canvas.drawCircle(circleX, circleY, circleRadius + 2f, highlightPaint);
    }

    private void drawSineWave(@NonNull Canvas canvas) { // Added @NonNull
        wavePath.reset();

        float centerY = getHeight() / 2f;
        float width = getWidth();

        wavePath.moveTo(0, centerY);

        for (float x = 0; x <= width; x += 5) {
            float y = centerY + waveAmplitude
                    * (float) Math.sin(x * waveFrequency + phase);
            wavePath.lineTo(x, y);
        }

        canvas.drawPath(wavePath, wavePaint);

        // Draw a second, thinner wave for depth
        Paint subtleWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subtleWavePaint.setColor(Color.parseColor("#E0F2F1")); // very light teal
        subtleWavePaint.setStyle(Paint.Style.STROKE);
        subtleWavePaint.setStrokeWidth(1.5f);
        subtleWavePaint.setStrokeCap(Paint.Cap.ROUND);

        Path subtleWavePath = new Path();
        subtleWavePath.moveTo(0, centerY);

        for (float x = 0; x <= width; x += 5) {
            float y = centerY + waveAmplitude * 0.7f
                    * (float) Math.sin(x * waveFrequency + phase + 0.5f);
            subtleWavePath.lineTo(x, y);
        }

        canvas.drawPath(subtleWavePath, subtleWavePaint);
    }

    public void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        animator.setDuration(60000); // 60 seconds
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);

        animator.addUpdateListener(animation -> {
            phase += 0.01f; // slow, calming motion
            invalidate();
        });

        animator.start();
    }

    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}