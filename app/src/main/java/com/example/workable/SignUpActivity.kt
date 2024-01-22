package com.example.workable

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        val textViewLoginPrompt: TextView = findViewById(R.id.textViewLoginPrompt)
        val spannableString = SpannableString("Already have an account? Login")
            val blackSpan = ForegroundColorSpan(Color.parseColor("#010F19")) // for colored text
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // if you want login underlined
                ds.color = Color.BLACK // text color
            }
        }

        spannableString.setSpan(blackSpan, 25, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, 25, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewLoginPrompt.text = spannableString
        textViewLoginPrompt.movementMethod = LinkMovementMethod.getInstance()



    }

}