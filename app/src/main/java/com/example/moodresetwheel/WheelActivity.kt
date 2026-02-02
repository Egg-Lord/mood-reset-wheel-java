package com.example.moodresetwheel

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random

class WheelActivity : AppCompatActivity() {

    private var feelingLevel = 0
    private var spinning = false
    private var currentRotation = 0f

    private lateinit var wheelContainer: FrameLayout
    private lateinit var labelTop: TextView
    private lateinit var labelBottom: TextView
    private var statusText: TextView? = null

    // Tick sound
    private var soundPool: SoundPool? = null
    private var tickSoundId = 0
    private var soundsReady = false

    // Pulse animators
    private var pulseTop: android.animation.ObjectAnimator? = null
    private var pulseBottom: android.animation.ObjectAnimator? = null

    companion object {
        private const val KEY_FEELING_LEVEL = FeelingLogActivity.KEY_FEELING_LEVEL
        private const val KEY_ACTIVITY_ID = "ACTIVITY_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wheel)

        if (!intent.hasExtra(KEY_FEELING_LEVEL)) {
            finish()
            return
        }

        feelingLevel = intent.getIntExtra(KEY_FEELING_LEVEL, 0)

        wheelContainer = findViewById(R.id.wheelContainer)
        labelTop = findViewById(R.id.labelTop)
        labelBottom = findViewById(R.id.labelBottom)
        statusText = runCatching { findViewById<TextView>(R.id.statusText) }.getOrNull()

        initTickSound()
        applyLabelsForFeeling(feelingLevel)
        startLabelPulse()

        statusText?.text = "Tap the wheel to spin"

        wheelContainer.setOnClickListener {
            if (!spinning) {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                spinWheel()
            }
        }
    }

    private fun applyLabelsForFeeling(level: Int) {
        when (level) {
            0 -> { labelTop.text = "Soft Sort";              labelBottom.text = "Ambient Tap Loop" }
            1 -> { labelTop.text = "Breathing Exercise";    labelBottom.text = "Breath Sync" }
            2 -> { labelTop.text = "Pattern Echo";          labelBottom.text = "Box Breathing" }
            3 -> { labelTop.text = "Hold & Release";        labelBottom.text = "Slow Drift" }
            else -> { labelTop.text = "Breathing Exercise"; labelBottom.text = "Breath Sync" }
        }
    }

    private fun startLabelPulse() {
        fun makePulse(view: View): android.animation.ObjectAnimator {
            return android.animation.ObjectAnimator.ofPropertyValuesHolder(
                view,
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.10f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.10f)
            ).apply {
                duration = 650
                repeatMode = android.animation.ObjectAnimator.REVERSE
                repeatCount = android.animation.ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        }
        pulseTop = makePulse(labelTop).also { it.start() }
        pulseBottom = makePulse(labelBottom).also { it.start() }
    }

    private fun stopLabelPulse() {
        pulseTop?.cancel()
        pulseBottom?.cancel()
        pulseTop = null
        pulseBottom = null
        labelTop.scaleX = 1f; labelTop.scaleY = 1f
        labelBottom.scaleX = 1f; labelBottom.scaleY = 1f
    }

    private fun initTickSound() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()

        tickSoundId = soundPool!!.load(this, R.raw.wheel_tick, 1)
        soundPool!!.setOnLoadCompleteListener { _, _, _ ->
            soundsReady = true
        }
    }

    private fun spinWheel() {
        spinning = true
        stopLabelPulse()
        statusText?.text = "Spinning…"

        val start = currentRotation
        val extraSpins = 4 * 360f
        val offset = Random.nextInt(0, 360).toFloat()
        val end = start + extraSpins + offset

        var lastTickStep = start.toInt()

        val anim = wheelContainer.animate()
            .rotation(end)
            .setDuration(2600)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .withEndAction {
                currentRotation = wheelContainer.rotation
                val chosenTop = isTopSelected(currentRotation)
                val chosenName = if (chosenTop) labelTop.text.toString() else labelBottom.text.toString()
                statusText?.text = "Selected: $chosenName"
                routeToActivity(chosenTop)
                spinning = false
            }

        wheelContainer.animate().setUpdateListener {
            currentRotation = wheelContainer.rotation
            val step = currentRotation.toInt()
            if (abs(step - lastTickStep) >= 18) {
                lastTickStep = step
                if (soundsReady) soundPool?.play(tickSoundId, 0.9f, 0.9f, 1, 0, 1f)
            }
        }

        anim.start()
    }

    private fun isTopSelected(rotation: Float): Boolean {
        val normalized = ((rotation % 360) + 360) % 360
        return normalized < 180f
    }

    private fun routeToActivity(topSelected: Boolean) {
        // Keep IDs consistent with your existing system
        val activityId = when (feelingLevel) {
            0 -> if (topSelected) "SOFT_SORT" else "AMBIENT_TAP_LOOP"
            1 -> if (topSelected) "BREATHING_EXERCISE" else "BREATH_SYNC"
            2 -> if (topSelected) "PATTERN_ECHO" else "BOX_BREATHING"
            3 -> if (topSelected) "HOLD_AND_RELEASE" else "SLOW_DRIFT"
            else -> "BREATHING_EXERCISE"
        }

        val nextIntent = when (activityId) {
            // ✅ Tap the Calm removed: top option now opens BreathingActivity too
            "BREATHING_EXERCISE", "BREATH_SYNC", "BOX_BREATHING", "HOLD_AND_RELEASE" ->
                android.content.Intent(this, BreathingActivity::class.java)

            else ->
                android.content.Intent(this, PlaceholderActivity::class.java)
        }.apply {
            putExtra(KEY_FEELING_LEVEL, feelingLevel)
            putExtra(KEY_ACTIVITY_ID, activityId)
        }

        startActivity(nextIntent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool?.release()
        soundPool = null
    }
}