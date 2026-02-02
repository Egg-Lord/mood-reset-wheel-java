package com.example.moodresetwheel

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedName = prefs.getString("username", null)

        if (!savedName.isNullOrBlank()) {
            goToMain()
            return
        }

        // ✅ Use your actual welcome layout
        setContentView(R.layout.activity_welcome)

        val etName = findViewById<TextInputEditText>(R.id.etUsername)
        val btnContinue = findViewById<MaterialButton>(R.id.btnContinue)

        btnContinue.setOnClickListener { v ->
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

            val name = etName.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                Toast.makeText(this, "Please enter your username.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().putString("username", name).apply()
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, FeelingLogActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
