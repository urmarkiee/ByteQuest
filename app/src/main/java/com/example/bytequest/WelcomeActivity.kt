package com.example.bytequest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

/** Welcome screen inspired by the user-provided ByteQuest neon reference. */
class WelcomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        findViewById<Button>(R.id.getStarted).setOnClickListener {
            openGame()
        }
    }
    private fun openGame() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
