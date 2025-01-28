package com.example.myapp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class aboutUs : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us) // Replace with your actual layout file name

        // Find the Home button
        val hButton: Button = findViewById(R.id.btn_home)
        hButton.setOnClickListener {
            Log.d("life", "Home button clicked, navigating back to MainActivity")
            val intent = Intent(this, MainActivity2::class.java) // Update with the correct target activity
            startActivity(intent)
        }


    }
}
