package com.example.workable

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val signupButton: Button = findViewById(R.id.signupButton)
        signupButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}