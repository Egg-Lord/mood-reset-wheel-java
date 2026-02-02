package com.example.moodresetwheel

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class FeelingLogActivity : AppCompatActivity() {

    companion object {
        const val KEY_FEELING_LEVEL = "FEELING_LEVEL"
    }

    // 0 = Neutral
    // 1 = Tense
    // 2 = Stressed
    // 3 = Overwhelmed
    private var selectedFeeling = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeling_log)

        val feelingSeekBar = findViewById<SeekBar>(R.id.feelingSeekBar)
        val continueButton = findViewById<MaterialButton>(R.id.continueButton)

        feelingSeekBar.max = 3
        feelingSeekBar.progress = 0

        feelingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedFeeling = progress.coerceIn(0, 3)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        continueButton.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            startActivity(Intent(this, WheelActivity::class.java).apply {
                putExtra(KEY_FEELING_LEVEL, selectedFeeling)
            })

            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}