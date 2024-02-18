package com.example.workable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.example.workable.R

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val back: ImageButton = findViewById(R.id.back)
        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val home: ImageButton = findViewById(R.id.briefcaseIcon)
        home.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


    }
}