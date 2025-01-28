package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val homeButton: Button = findViewById(R.id.btn_home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val registerButton: Button = findViewById(R.id.btn_register)
        registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val firstNameEditText: EditText = findViewById(R.id.firstName)
        val lastNameEditText: EditText = findViewById(R.id.lastName)
        val emailEditText: EditText = findViewById(R.id.email)
        val passwordEditText: EditText = findViewById(R.id.Password)
        val radioGroupGender: RadioGroup = findViewById(R.id.radioGroupGender)

        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val selectedGenderId = radioGroupGender.checkedRadioButtonId
        val gender = when (selectedGenderId) {
            R.id.radioMale -> "Male"
            R.id.radioFemale -> "Female"
            R.id.radioOther -> "Other"
            else -> ""
        }

        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Send email verification
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Registration successful! Please verify your email.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Store user info in Firebase Database
                                val userId = user.uid
                                val database = FirebaseDatabase.getInstance()
                                val userRef = database.reference.child("users").child(userId)

                                val userInfo = mapOf(
                                    "fullName" to "$firstName $lastName",
                                    "gender" to gender,
                                    "email" to email,
                                    "verified" to false
                                )

                                userRef.setValue(userInfo).addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        auth.signOut() // Log out the user until they verify
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        val error = dbTask.exception?.message
                                        Toast.makeText(this, "Error saving user info: $error", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                val error = verificationTask.exception?.message
                                Toast.makeText(this, "Failed to send verification email: $error", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    val error = task.exception?.message
                    Toast.makeText(this, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
