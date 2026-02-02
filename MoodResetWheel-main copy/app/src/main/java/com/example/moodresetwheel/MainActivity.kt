package com.example.moodresetwheel

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val savedName = prefs.getString("username", null)

        val next = if (savedName.isNullOrBlank()) {
            Intent(this, WelcomeActivity::class.java)
        } else {
            Intent(this, FeelingLogActivity::class.java)
        }

        startActivity(next)
        finish()
    }
}
