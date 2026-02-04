package com.example.moodresetwheel;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class PizzaWheelView extends View {

    private final Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final String[] labels = {
            "Soft Sort",
            "Ambient Tap",
            "Tap The Calm",
            "Breathing",
            "Pattern Echo",
            "Box Breathing",
            "Hold & Release",
            "Slow Drift"
    };

    private final int[] colors = {
            Color.parseColor("#F8BBD0"),
            Color.parseColor("#B2EBF2"),
            Color.parseColor("#C8E6C9"),
            Color.parseColor("#FFF9C4"),
            Color.parseColor("#D1C4E9"),
            Color.parseColor("#FFCCBC"),
            Color.parseColor("#BBDEFB"),
            Color.parseColor("#DCEDC8")
    };

    public PizzaWheelView(Context c, AttributeSet a) {
        super(c, a);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float r = Math.min(w, h) / 2f;

        RectF oval = new RectF(
                w/2 - r, h/2 - r,
                w/2 + r, h/2 + r
        );

        float angle = 360f / 8f;

        for (int i = 0; i < 8; i++) {

            slicePaint.setColor(colors[i]);

            canvas.drawArc(
                    oval,
                    i * angle,
                    angle,
                    true,
                    slicePaint
            );

            drawLabel(canvas, labels[i], i, r);
        }
    }

    private void drawLabel(Canvas c, String text, int i, float r) {

        float angle = (float) Math.toRadians((i * 45) + 22.5);
        float x = getWidth()/2 + (float)(r * 0.6 * Math.cos(angle));
        float y = getHeight()/2 + (float)(r * 0.6 * Math.sin(angle));

        c.drawText(text, x, y, textPaint);
    }
}
