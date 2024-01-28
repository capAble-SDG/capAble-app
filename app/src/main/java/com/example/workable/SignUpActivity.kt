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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity: AppCompatActivity() {

    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        val fullNameEditText: EditText = findViewById(R.id.fullName)
        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPassword)
        val signupButton: Button = findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (fullName.isEmpty()) {
                fullNameEditText.error = "Please enter your full name"
                fullNameEditText.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.error = "Please confirm your password"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Passwords do not match"
                confirmPasswordEditText.requestFocus()
                return@setOnClickListener
            }

            createUser(fullName, email, password)
            startActivity(Intent(this, MainActivity::class.java))
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
                ds.isUnderlineText = true //to make the login underlined
                ds.color = Color.BLACK 
            }
        }

        spannableString.setSpan(blackSpan, 25, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, 25, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewLoginPrompt.text = spannableString
        textViewLoginPrompt.movementMethod = LinkMovementMethod.getInstance()

    }
    private fun createUser(fullName: String, email: String, password: String) {
        val user = hashMapOf(
            "fullName" to fullName,
            "email" to email,
            "password" to password
        )

        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                println("User added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                println("Error adding user $e")
            }
    }

}