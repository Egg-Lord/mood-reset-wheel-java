package com.example.moodresetwheel

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BreathingActivity : AppCompatActivity() {

    private lateinit var breathCircle: View
    private lateinit var tvPhase: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnCenter: MaterialButton
    private lateinit var btnSlow: MaterialButton
    private lateinit var btnNormal: MaterialButton
    private lateinit var btnFast: MaterialButton

    private var isRunning = false
    private var phaseTimer: CountDownTimer? = null
    private var animatorSet: AnimatorSet? = null

    private var cycleCount = 0
    private val maxCycles = 3

    private enum class SpeedMode { SLOW, NORMAL, FAST }
    private var speedMode = SpeedMode.NORMAL

    private data class Pattern(val inhale: Int, val hold1: Int, val exhale: Int, val hold2: Int)

    private fun patternFor(mode: SpeedMode): Pattern = when (mode) {
        SpeedMode.SLOW -> Pattern(inhale = 5, hold1 = 2, exhale = 6, hold2 = 2)
        SpeedMode.NORMAL -> Pattern(inhale = 4, hold1 = 2, exhale = 4, hold2 = 2)
        SpeedMode.FAST -> Pattern(inhale = 3, hold1 = 1, exhale = 3, hold2 = 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breathing)

        breathCircle = findViewById(R.id.breathCircle)
        tvPhase = findViewById(R.id.tvPhase)
        tvTimer = findViewById(R.id.tvTimer)
        btnCenter = findViewById(R.id.btnBreathStart)
        btnSlow = findViewById(R.id.btnSlow)
        btnNormal = findViewById(R.id.btnNormal)
        btnFast = findViewById(R.id.btnFast)

        setSpeed(SpeedMode.NORMAL)
        setReadyUI()

        btnCenter.setOnClickListener {
            if (isRunning) stopBreathing(goToComplete = true) else startBreathing()
        }

        btnSlow.setOnClickListener { if (!isRunning) setSpeed(SpeedMode.SLOW) }
        btnNormal.setOnClickListener { if (!isRunning) setSpeed(SpeedMode.NORMAL) }
        btnFast.setOnClickListener { if (!isRunning) setSpeed(SpeedMode.FAST) }
    }

    private fun setSpeed(mode: SpeedMode) {
        speedMode = mode

        btnSlow.isEnabled = mode != SpeedMode.SLOW
        btnNormal.isEnabled = mode != SpeedMode.NORMAL
        btnFast.isEnabled = mode != SpeedMode.FAST

        if (!isRunning) {
            tvTimer.text = when (mode) {
                SpeedMode.SLOW -> getString(R.string.breathing_speed_slow)
                SpeedMode.NORMAL -> getString(R.string.breathing_speed_normal)
                SpeedMode.FAST -> getString(R.string.breathing_speed_fast)
            }
        }
    }

    private fun setReadyUI() {
        tvPhase.text = getString(R.string.breathing_ready)
        tvTimer.text = getString(R.string.breathing_tap_to_start)

        btnCenter.text = getString(R.string.start)
        btnCenter.setIconResource(android.R.drawable.ic_media_play)

        breathCircle.scaleX = 0.78f
        breathCircle.scaleY = 0.78f
        breathCircle.alpha = 1f
    }

    private fun startBreathing() {
        isRunning = true
        cycleCount = 0

        btnCenter.text = getString(R.string.stop)
        btnCenter.setIconResource(android.R.drawable.ic_media_pause)

        // lock pace buttons while running
        btnSlow.isEnabled = false
        btnNormal.isEnabled = false
        btnFast.isEnabled = false

        runCycle()
    }

    private fun runCycle() {
        if (!isRunning) return
        val p = patternFor(speedMode)

        // Inhale
        runPhase(
            label = getString(R.string.breathing_inhale),
            seconds = p.inhale,
            animate = { animateCircle(toScale = 1.06f, durationMs = p.inhale * 1000L) }
        ) {
            // Hold
            runPhase(
                label = getString(R.string.breathing_hold),
                seconds = p.hold1,
                animate = { /* no animation */ }
            ) {
                // Exhale
                runPhase(
                    label = getString(R.string.breathing_exhale),
                    seconds = p.exhale,
                    animate = { animateCircle(toScale = 0.78f, durationMs = p.exhale * 1000L) }
                ) {
                    // Hold
                    runPhase(
                        label = getString(R.string.breathing_hold),
                        seconds = p.hold2,
                        animate = { /* no animation */ }
                    ) {
                        // ✅ Count cycles
                        cycleCount += 1
                        if (cycleCount >= maxCycles) {
                            stopBreathing(goToComplete = true)
                        } else {
                            runCycle()
                        }
                    }
                }
            }
        }
    }

    private fun runPhase(label: String, seconds: Int, animate: () -> Unit, onDone: () -> Unit) {
        if (!isRunning) return

        tvPhase.text = label
        animate()

        phaseTimer?.cancel()
        phaseTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = (millisUntilFinished / 1000L).toInt() + 1
                tvTimer.text = getString(R.string.breathing_seconds, remaining)
            }

            override fun onFinish() {
                tvTimer.text = getString(R.string.breathing_seconds, 0)
                onDone()
            }
        }.start()
    }

    private fun animateCircle(toScale: Float, durationMs: Long) {
        animatorSet?.cancel()

        val scaleX = ObjectAnimator.ofFloat(breathCircle, View.SCALE_X, breathCircle.scaleX, toScale)
        val scaleY = ObjectAnimator.ofFloat(breathCircle, View.SCALE_Y, breathCircle.scaleY, toScale)

        animatorSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopBreathing(goToComplete: Boolean) {
        isRunning = false
        phaseTimer?.cancel()
        phaseTimer = null

        animatorSet?.cancel()
        animatorSet = null

        // unlock pace buttons
        setSpeed(speedMode)

        if (goToComplete) {
            goToCompleteScreen()
        } else {
            setReadyUI()
        }
    }

    private fun goToCompleteScreen() {
        val feelingLevel = intent.getIntExtra(FeelingLogActivity.KEY_FEELING_LEVEL, 0)
        val activityId = intent.getStringExtra("ACTIVITY_ID") ?: "UNKNOWN"

        startActivity(Intent(this, BreathingCompleteActivity::class.java).apply {
            putExtra(FeelingLogActivity.KEY_FEELING_LEVEL, feelingLevel)
            putExtra("ACTIVITY_ID", activityId)
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onStop() {
        super.onStop()
        // if user leaves screen, stop quietly (no completion navigation)
        if (isRunning) stopBreathing(goToComplete = false)
    }
}