package com.example.workable

import android.content.Context
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity: AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        val googleSignInButton: Button = findViewById(R.id.google)
        googleSignInButton.setOnClickListener {
            googleSignIn()
        }

        db = FirebaseFirestore.getInstance()

        val backButton: ImageButton = findViewById(R.id.back)
        backButton.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }

        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.signupButton)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty()) {
                emailEditText.error = "Please enter your email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Please enter your password"
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        val textViewLoginPrompt: TextView = findViewById(R.id.textViewSignUpPrompt)
        val spannableString = SpannableString("Don't have an account? Sign Up")

        val blackSpan = ForegroundColorSpan(Color.parseColor("#010F19")) // for colored text
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true // if you want login underlined
                ds.color = Color.BLACK // text color
            }
        }

        spannableString.setSpan(blackSpan, 23, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(clickableSpan, 23, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textViewLoginPrompt.text = spannableString
        textViewLoginPrompt.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun loginUser(email: String, password: String) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userPassword = document.getString("password")
                    if (password == userPassword) {
                        val intent = Intent(this, MainActivity::class.java)
                        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                        with (sharedPref.edit()) {
                            putBoolean("isLoggedIn", true)
                            apply()
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show()
                    }
                }
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error logging in: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("459974671569-321sc64ilda12fu7l1k5tuck6k76md7b.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                Toast.makeText(this, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
            }
        }
    }


    companion object {
        private const val RC_SIGN_IN = 9001
    }


}