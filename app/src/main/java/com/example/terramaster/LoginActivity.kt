package com.example.terramaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        tvSignUp = findViewById(R.id.tvSignUp)
        progressBar = findViewById(R.id.progressBar)

        tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnSignIn.setOnClickListener {
            validateAndLoginUser()
        }
    }

    private fun validateAndLoginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Valid Email is required"
            return
        }

        if (password.isEmpty() || password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSignIn.isEnabled = false

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                if (user != null && user.isEmailVerified) {
                    updateUserStatusAndToken(user.uid)
                } else {
                    progressBar.visibility = View.GONE
                    btnSignIn.isEnabled = true
                    Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show()
                }
            } else {
                progressBar.visibility = View.GONE
                btnSignIn.isEnabled = true
                Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserStatusAndToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val fcmToken = tokenTask.result
                db.collection("users").document(userId)
                    .update(mapOf("status" to "Active", "fcmToken" to fcmToken))
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        btnSignIn.isEnabled = true
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        navigateToMainActivity()
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        btnSignIn.isEnabled = true
                        Log.e("LoginActivity", "Error updating user status/token: ${e.message}")
                        Toast.makeText(this, "Error updating user data.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                progressBar.visibility = View.GONE
                btnSignIn.isEnabled = true
                Log.e("LoginActivity", "Error fetching FCM token: ${tokenTask.exception?.message}")
                Toast.makeText(this, "Error fetching FCM token.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close LoginActivity so the user cannot return to it
    }
}
