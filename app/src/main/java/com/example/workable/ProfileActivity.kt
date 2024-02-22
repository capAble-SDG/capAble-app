package com.example.workable

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.example.workable.R
import com.google.gson.Gson

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val username: TextView= findViewById(R.id.txtUserName)
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("fullName", "")
        username.text = fullName

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