package com.example.moodresetwheel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RippleFieldView extends View {

    public interface Listener {
        void onAllCleared();
    }

    private static class RippleTarget {
        float x, y;
        float baseR;
        int shape; // 0=circle,1=ring,2=roundedRect,3=diamond
        boolean popped;
        long popStartMs;
        boolean held;
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random rng = new Random();
    private final List<RippleTarget> targets = new ArrayList<>();
    private Vibrator vibrator;

    private Listener listener;

    // Tuning values
    private int targetCount = 12;
    private long popDurationMs = 1200; // Slower animation
    private float minR = 66f; // 3x bigger
    private float maxR = 114f; // 3x bigger

    public RippleFieldView(Context context) {
        super(context);
        init();
    }

    public RippleFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RippleFieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickable(true);
        paint.setStyle(Paint.Style.FILL);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void startNewGame(int count) {
        targetCount = Math.max(1, count);
        targets.clear();

        if (getWidth() == 0 || getHeight() == 0) {
            invalidate();
            return;
        }

        float padding = dp(10);
        int attempts = 1500;
        for (int i = 0; i < targetCount && attempts > 0; ) {
            attempts--;

            float r = lerp(minR, maxR, rng.nextFloat());
            float x = padding + r + rng.nextFloat() * (getWidth() - 2 * (padding + r));
            float y = padding + r + rng.nextFloat() * (getHeight() - 2 * (padding + r));

            if (!overlapsExisting(x, y, r)) {
                RippleTarget t = new RippleTarget();
                t.x = x;
                t.y = y;
                t.baseR = r;
                t.shape = rng.nextInt(4);
                t.popped = false;
                t.popStartMs = 0;
                t.held = false;
                targets.add(t);
                i++;
            }
        }

        invalidate();
    }

    private boolean overlapsExisting(float x, float y, float r) {
        float minDist = (r * 2.2f);
        for (RippleTarget t : targets) {
            float dx = x - t.x;
            float dy = y - t.y;
            float dist2 = dx * dx + dy * dy;
            float req = (t.baseR + r) * 1.15f + minDist * 0.15f;
            if (dist2 < req * req) return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = SystemClock.uptimeMillis();

        for (RippleTarget t : targets) {
            if (t.held) {
                float p = clamp01((now - t.popStartMs) / (float) popDurationMs);
                float screenFill = lerp(t.baseR, getWidth() * 1.5f, easeOut(p));
                drawTarget(canvas, t, screenFill / t.baseR, 1f);

                if (p >= 1f) {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    t.held = false;
                    t.popped = true;
                    t.popStartMs = now;
                }
            } else if (!t.popped) {
                drawTarget(canvas, t, 1f, 1f);
            } else {
                float p = clamp01((now - t.popStartMs) / (float) popDurationMs);
                float alpha = lerp(1f, 0f, p);
                float scale = lerp(1f, 2.15f, easeOut(p));
                drawTarget(canvas, t, scale, alpha);
            }
        }

        boolean removedAny = false;
        Iterator<RippleTarget> it = targets.iterator();
        while (it.hasNext()) {
            RippleTarget t = it.next();
            if (t.popped && (now - t.popStartMs) >= popDurationMs) {
                it.remove();
                removedAny = true;
            }
        }

        if (removedAny && targets.isEmpty() && listener != null) {
            listener.onAllCleared();
        }

        if (hasAnimatingTargets()) {
            postInvalidateOnAnimation();
        }
    }

    private void drawTarget(Canvas canvas, RippleTarget t, float scale, float alpha) {
        float r = t.baseR * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setARGB((int) (alpha * 70), 160, 210, 255);

        switch (t.shape) {
            case 1: // ring
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(Math.max(2f, r * 0.18f));
                canvas.drawCircle(t.x, t.y, r, paint);
                break;
            case 2: { // rounded rect
                paint.setStyle(Paint.Style.FILL);
                RectF rect = new RectF(t.x - r, t.y - r, t.x + r, t.y + r);
                canvas.drawRoundRect(rect, r * 0.35f, r * 0.35f, paint);
                break;
            }
            case 3: { // diamond
                paint.setStyle(Paint.Style.FILL);
                Path p = new Path();
                p.moveTo(t.x, t.y - r);
                p.lineTo(t.x + r, t.y);
                p.lineTo(t.x, t.y + r);
                p.lineTo(t.x - r, t.y);
                p.close();
                canvas.drawPath(p, paint);
                break;
            }
            default: // circle
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(t.x, t.y, r, paint);
                break;
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setARGB((int) (alpha * 160), 210, 240, 255);
        canvas.drawCircle(t.x, t.y, r * 0.42f, paint);
    }

    private boolean hasAnimatingTargets() {
        for (RippleTarget t : targets) {
            if (t.popped || t.held) return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float tx = event.getX();
        float ty = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                RippleTarget hit = findTarget(tx, ty);
                if (hit != null) {
                    hit.held = true;
                    hit.popStartMs = SystemClock.uptimeMillis();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                for (RippleTarget t : targets) {
                    if (t.held) {
                        long elapsed = SystemClock.uptimeMillis() - t.popStartMs;
                        if (elapsed < popDurationMs) {
                            t.held = false;
                        }
                    }
                }
                vibrator.cancel();
                invalidate();
                return true;
        }

        return true;
    }

    private RippleTarget findTarget(float tx, float ty) {
        RippleTarget hit = null;
        float bestDist2 = Float.MAX_VALUE;
        for (RippleTarget t : targets) {
            if (t.popped) continue;
            float dx = tx - t.x;
            float dy = ty - t.y;
            float dist2 = dx * dx + dy * dy;
            if (dist2 <= (t.baseR * t.baseR) && dist2 < bestDist2) {
                bestDist2 = dist2;
                hit = t;
            }
        }
        return hit;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float clamp01(float x) {
        return Math.max(0f, Math.min(1f, x));
    }

    private float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }
}