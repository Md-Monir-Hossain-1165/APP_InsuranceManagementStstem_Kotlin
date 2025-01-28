package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Declare FirebaseAuth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Find buttons by ID
        val loginButton: Button = findViewById(R.id.btn_login)
        val registerButton: Button = findViewById(R.id.btn_register)

        // Set up the click listener for login button
        loginButton.setOnClickListener {
            Log.d("MainActivity", "Login button clicked")

            // Attempt login
            loginUser()
        }

        // Set up the click listener for register button
        registerButton.setOnClickListener {
            Log.d("MainActivity", "Register button clicked")

            // Navigate to RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        // Find EditText fields for email and password
        val emailEditText: EditText = findViewById(R.id.Email)
        val passwordEditText: EditText = findViewById(R.id.password)

        // Retrieve user input
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Validate input fields
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Check for admin login
        if (email == "admin@gmail.com" && password == "123456") {
            // Navigate to AdminActivity
            Log.d("MainActivity", "Admin login successful, navigating to AdminActivity")
            Toast.makeText(this, "Admin login successful!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, admin::class.java)
            startActivity(intent)
            finish() // Close current activity
            return
        }

        // Authenticate user using FirebaseAuth for normal users
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check if email is verified
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        // Login successful and email verified, navigate to MainActivity2
                        Log.d("MainActivity", "Login successful and email verified, navigating to MainActivity2")
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity2::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Email not verified
                        Toast.makeText(
                            this,
                            "Please verify your email before logging in",
                            Toast.LENGTH_SHORT
                        ).show()
                        auth.signOut() // Ensure the user is signed out
                    }
                } else {
                    // Login failed, display error
                    val errorMessage = task.exception?.message ?: "Authentication failed"
                    Log.e("MainActivity", "Login failed: $errorMessage")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}
