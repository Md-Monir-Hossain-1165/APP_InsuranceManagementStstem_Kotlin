package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class details : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Get the current user's UID
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        val welcomeTextView: TextView = findViewById(R.id.tv_welcome_user)

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.reference.child("users").child(userId)

            userRef.child("fullName").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fullName = task.result?.value as? String ?: "User"
                    welcomeTextView.text = "Welcome, $fullName!"
                    Toast.makeText(this, "Welcome, $fullName!", Toast.LENGTH_SHORT).show()
                } else {
                    welcomeTextView.text = "Welcome, User!"
                    Toast.makeText(this, "Failed to load user details.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            welcomeTextView.text = "Welcome, User!"
            Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show()
        }

        // Set up the Home button listener
        val homeButton: Button = findViewById(R.id.btn_home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }
        val lifeButton: Button = findViewById(R.id.button_life)

        // Set up the click listener
        lifeButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating back to Payment")


            val intent = Intent(this, life::class.java)
            startActivity(intent)
        }
    }
}
