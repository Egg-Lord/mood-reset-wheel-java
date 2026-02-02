package com.example.moodresetwheel

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlaceholderActivity : AppCompatActivity() {

    private var player: MediaPlayer? = null
    private var feelingLevel = 0
    private var activityId: String = "UNKNOWN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_mini)

        feelingLevel = intent.getIntExtra(FeelingLogActivity.KEY_FEELING_LEVEL, 0)
        activityId = intent.getStringExtra("ACTIVITY_ID") ?: "UNKNOWN"

        val title = findViewById<TextView>(R.id.activityTitle)
        val preview = findViewById<FrameLayout>(R.id.previewArea)
        val startButton = findViewById<Button>(R.id.startButton)
        val spinAgainButton = findViewById<Button>(R.id.spinAgainButton)
        val exitButton = findViewById<Button>(R.id.exitButton)

        title.text = prettyName(activityId)

        preview.removeAllViews()
        val msg = TextView(this).apply {
            text = "This activity is coming soon.\n\nTry the Breathing Exercise for now 🌿"
            setTextColor(0xFF2B2A28.toInt())
            textSize = 16f
            gravity = Gravity.CENTER
        }
        preview.addView(msg)

        spinAgainButton.visibility = View.GONE
        exitButton.visibility = View.GONE

        startButton.setOnClickListener {

            // ✅ Route breathing-related activities to BreathingActivity
            if (activityId == "BREATHING_EXERCISE" ||
                activityId == "BREATH_SYNC" ||
                activityId == "BOX_BREATHING" ||
                activityId == "HOLD_AND_RELEASE"
            ) {
                startActivity(Intent(this, BreathingActivity::class.java).apply {
                    putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel)
                    putExtra("ACTIVITY_ID", activityId)
                })
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
                return@setOnClickListener
            }

            startButton.animate()
                .scaleX(0.97f).scaleY(0.97f)
                .setDuration(90)
                .withEndAction {
                    startButton.animate().scaleX(1f).scaleY(1f).setDuration(140)
                        .setInterpolator(OvershootInterpolator())
                        .start()
                }.start()

            player?.release()
            player = MediaPlayer.create(this, R.raw.calm_music).apply {
                isLooping = true
                setVolume(0.55f, 0.55f)
                start()
            }

            startButton.visibility = View.GONE
            spinAgainButton.visibility = View.VISIBLE
            exitButton.visibility = View.VISIBLE

            msg.text = "This activity is coming soon.\n\nTry the Breathing Exercise for now 🌿"
        }

        spinAgainButton.setOnClickListener {
            player?.stop()
            player?.release()
            player = null

            startActivity(Intent(this, WheelActivity::class.java).apply {
                putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel)
            })
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        exitButton.setOnClickListener { finishAffinity() }
    }

    private fun prettyName(id: String): String {
        return when (id) {
            "SOFT_SORT" -> "Soft Sort"
            "AMBIENT_TAP_LOOP" -> "Ambient Tap Loop"
            "BREATHING_EXERCISE" -> "Breathing Exercise"
            "BREATH_SYNC" -> "Breath Sync"
            "PATTERN_ECHO" -> "Pattern Echo"
            "BOX_BREATHING" -> "Box Breathing"
            "HOLD_AND_RELEASE" -> "Hold & Release"
            "SLOW_DRIFT" -> "Slow Drift"
            else -> "Mini Activity"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
        player = null
    }
}