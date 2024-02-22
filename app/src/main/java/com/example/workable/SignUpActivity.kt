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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class SignUpActivity: AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth:FirebaseAuth

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
            val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putBoolean("isLoggedIn", true)
                apply()
            }
            startActivity(Intent(this, MainActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()

        val googleSignInButton: Button = findViewById(R.id.google)
        googleSignInButton.setOnClickListener {
            googleSignIn()
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
                // get the user info from the authenticated user
                val firebaseUser = auth.currentUser
                val name = firebaseUser?.displayName ?: ""
                val email = firebaseUser?.email ?: ""

                val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("fullName", name)
                    apply()
                }

                // check if user already exists
                checkUserInFirestore(name, email)
            } else {
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserInFirestore(fullName: String, email: String) {
        db.collection("users").whereEqualTo("email", email).limit(1).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // since user doesn't exist, create new
                    addUserToFirestore(fullName, email)
                } else {
                    val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                    with (sharedPref.edit()) {
                        putBoolean("isLoggedIn", true)
                        apply()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error checking user: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addUserToFirestore(fullName: String, email: String) {
        val user = hashMapOf("fullName" to fullName, "email" to email)
        db.collection("users").add(user)
            .addOnSuccessListener {
                val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding user to Firestore: $e", Toast.LENGTH_SHORT).show()
            }
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
                val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("fullName", fullName)
                    apply()
                }
            }
            .addOnFailureListener { e ->
                println("Error adding user $e")
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }


}