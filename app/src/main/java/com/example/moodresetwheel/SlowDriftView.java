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

public class SlowDriftView extends View {

    private Paint circlePaint;
    private Paint wavePaint;
    private Path wavePath;

    private float circleX;
    private float circleY;
    private float circleRadius = 40f;

    private float waveAmplitude;
    private float waveFrequency = 0.02f;
    private float phase = 0f;

    private ValueAnimator animator;

    public SlowDriftView(Context context) {
        super(context);
        init();
    }

    public SlowDriftView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Circle paint (soft blue)
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#64B5F6"));
        circlePaint.setStyle(Paint.Style.FILL);

        // Wave paint (light gray)
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setColor(Color.parseColor("#B0BEC5"));
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(3f);

        wavePath = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        waveAmplitude = h * 0.15f;
        circleX = w / 2f;     //  center horizontally
        circleY = h / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (getWidth() == 0 || getHeight() == 0) return;

        drawSineWave(canvas);

        float centerY = getHeight() / 2f;
        circleY = centerY + waveAmplitude
                * (float) Math.sin(circleX * waveFrequency + phase);

        canvas.drawCircle(circleX, circleY, circleRadius, circlePaint);
    }

    private void drawSineWave(Canvas canvas) {
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