package com.example.myapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.util.Log

class home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Your RegisterActivity XML layout

        val yesButton: Button = findViewById(R.id.btnYes)

        // Set up the click listener
        yesButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating back to Payment")


            val intent = Intent(this, payment::class.java)
            startActivity(intent)
        }
        // Find the Home button by its ID
        val hButton: Button = findViewById(R.id.btn_home)

        // Set up the click listener
        hButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Home button clicked, navigating back to MainActivity")

            // Navigate back to MainActivity
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }
}
