package com.example.moodresetwheel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BreathingCompleteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing_complete)

        val feelingLevel = intent.getIntExtra(FeelingLogActivity.KEY_FEELING_LEVEL, 0)
        val activityId = intent.getStringExtra("ACTIVITY_ID") ?: "UNKNOWN"

        val btnTryTask = findViewById<MaterialButton>(R.id.btnTryAnotherTask)
        val btnSpinAgain = findViewById<MaterialButton>(R.id.btnSpinAgain)

        // Choice C: Try another task (go to your mini activity placeholder)
        btnTryTask.setOnClickListener {
            startActivity(Intent(this, PlaceholderActivity::class.java).apply {
                putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel)
                putExtra("ACTIVITY_ID", nextAfterBreathing(activityId))
            })
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // Choice C: Spin again (back to wheel)
        btnSpinAgain.setOnClickListener {
            startActivity(Intent(this, WheelActivity::class.java).apply {
                putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel)
            })
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    /**
     * Decide what task comes after breathing.
     * You can change this mapping anytime.
     */
    private fun nextAfterBreathing(currentId: String): String {
        return when (currentId) {
            "BREATH_SYNC" -> "SOFT_SORT"
            "BOX_BREATHING" -> "PATTERN_ECHO"
            "HOLD_AND_RELEASE" -> "SLOW_DRIFT"
            "BREATHING_EXERCISE" -> "SOFT_SORT"
            else -> "SOFT_SORT"
        }
    }
}