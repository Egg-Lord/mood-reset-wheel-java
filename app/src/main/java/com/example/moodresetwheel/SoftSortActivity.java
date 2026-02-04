package com.example.moodresetwheel;

import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class SoftSortActivity extends AppCompatActivity {

    private final int[] ROUND_SET = {
            R.drawable.ic_round_circle, R.drawable.ic_round_oval,
            R.drawable.ic_round_cloud, R.drawable.ic_round_blob
    };

    private final int[] POINTY_SET = {
            R.drawable.ic_pointy_star, R.drawable.ic_pointy_triangle,
            R.drawable.ic_pointy_diamond, R.drawable.ic_pointy_hex
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_sort);

        setupTarget(findViewById(R.id.container_round), "ROUND");
        setupTarget(findViewById(R.id.container_pointy), "POINTY");

        loadGameItems();
    }

    private void loadGameItems() {
        GridLayout tray = findViewById(R.id.traySource);
        tray.removeAllViews();
        for (int resId : ROUND_SET) {
            createShape(tray, resId, "ROUND");
        }
        for (int resId : POINTY_SET) {
            createShape(tray, resId, "POINTY");
        }
    }

    private void createShape(GridLayout tray, int resId, String tag) {
        ImageView shape = new ImageView(this);
        shape.setImageResource(resId);
        shape.setTag(tag);

        shape.setRotation(new Random().nextInt(30) - 15);

        int size = (int) (52 * getResources().getDisplayMetrics().density);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = size;
        params.height = size;
        params.setMargins(14, 14, 14, 14);
        shape.setLayoutParams(params);

        shape.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Tactical "Pop" animation on pick-up
                v.animate().scaleX(1.15f).scaleY(1.15f).setDuration(100).start();

                View.DragShadowBuilder shadow = new View.DragShadowBuilder(v);
                v.startDragAndDrop(null, shadow, v, 0);
                v.setVisibility(View.INVISIBLE); // Hide original while dragging
                return true;
            }
            return false;
        });
        tray.addView(shape);
    }

    private void setupTarget(GridLayout basket, String acceptedTag) {
        basket.setOnDragListener((v, event) -> {
            View draggedView = (View) event.getLocalState();

            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    if (draggedView.getTag().equals(acceptedTag)) {
                        ((ViewGroup) draggedView.getParent()).removeView(draggedView);
                        ((GridLayout) v).addView(draggedView);
                        draggedView.setVisibility(View.VISIBLE);
                        // Reset scale for landing
                        draggedView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        checkEndCondition();
                        return true;
                    }
                    return false;

                case DragEvent.ACTION_DRAG_ENDED:
                    if (!event.getResult()) {
                        draggedView.setVisibility(View.VISIBLE);
                        draggedView.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    }
                    return true;
            }
            return true;
        });
    }

    private void checkEndCondition() {
        GridLayout tray = findViewById(R.id.traySource);

        if (tray.getChildCount() == 0) {
            tray.postDelayed(() -> {
                if (!isFinishing()) {
                    new AlertDialog.Builder(this)
                            .setTitle("✨ All Sorted! ✨")
                            .setMessage("Great job! You've sorted all the shapes! \n\n Still Feeling bored? Try again!")
                            .setPositiveButton("Finish", (d, w) -> finish())
                            .setNeutralButton("Restart 🔄", (d, w) -> {
                                ((GridLayout) findViewById(R.id.container_round)).removeAllViews();
                                ((GridLayout) findViewById(R.id.container_pointy)).removeAllViews();
                                loadGameItems();
                            })
                            .setCancelable(false)
                            .show();
                }
            }, 300);
        }
    }
}