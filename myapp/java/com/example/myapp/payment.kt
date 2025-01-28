package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class payment : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val spinnerPolicy: Spinner = findViewById(R.id.spinnerPolicy)
        val spinnerPaymentMethod: Spinner = findViewById(R.id.spinnerPaymentMethod)
        val paymentDetailsContainer: LinearLayout = findViewById(R.id.paymentDetailsContainer)
        val btnSubmit: Button = findViewById(R.id.btnSubmit)
        val homeButton: Button = findViewById(R.id.btn_home)

        // Initialize Firebase Auth and check user authentication
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(
                this,
                "User not logged in. Please log in to proceed.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Get user's sanitized email as the unique key
        val userEmail = currentUser.email ?: "unknown_user"
        val userKey = userEmail.replace(".", "_")

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("users/$userKey/payments")

        homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity2::class.java))
        }

        spinnerPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                paymentDetailsContainer.removeAllViews()
                when (position) {
                    1 -> addCreditCardFields(paymentDetailsContainer)
                    2 -> addNetBankingFields(paymentDetailsContainer)
                    3 -> addMobileBankingFields(paymentDetailsContainer)
                }
                paymentDetailsContainer.visibility = if (position > 0) View.VISIBLE else View.GONE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSubmit.setOnClickListener {
            val policy = spinnerPolicy.selectedItem?.toString() ?: "N/A"
            val amount = findViewById<EditText>(R.id.etPremiumAmount).text.toString().toDoubleOrNull()

            if (amount == null) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Retrieve current payment data
            val policyRef = database.child(policy)
            policyRef.get().addOnSuccessListener { snapshot ->
                val currentAmount = snapshot.child("amount").getValue(Double::class.java) ?: 0.0
                val updatedAmount = currentAmount + amount

                // Prepare updated payment data
                val paymentData = mapOf(
                    "amount" to updatedAmount,
                    "paymentMethod" to spinnerPaymentMethod.selectedItem.toString()
                )

                // Update the payment data in the database
                policyRef.setValue(paymentData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Payment submitted successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Failed to submit payment: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error retrieving payment info: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun addCreditCardFields(container: LinearLayout) {
        val cardNumber = EditText(this).apply {
            hint = "Card Number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val expiryDate = EditText(this).apply {
            hint = "Expiry Date (MM/YY)"
            inputType = android.text.InputType.TYPE_CLASS_DATETIME
        }
        val cvv = EditText(this).apply {
            hint = "CVV"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        container.addView(cardNumber)
        container.addView(expiryDate)
        container.addView(cvv)
    }

    private fun addNetBankingFields(container: LinearLayout) {
        val bankName = EditText(this).apply { hint = "Bank Name" }
        val userID = EditText(this).apply { hint = "User ID" }
        val password = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        container.addView(bankName)
        container.addView(userID)
        container.addView(password)
    }

    private fun addMobileBankingFields(container: LinearLayout) {
        val provider = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@payment,
                android.R.layout.simple_spinner_dropdown_item,
                listOf("Select Provider", "BKash", "Nagad")
            )
        }
        val mobileNumber = EditText(this).apply {
            hint = "Mobile Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        val pin = EditText(this).apply {
            hint = "PIN"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        container.addView(provider)
        container.addView(mobileNumber)
        container.addView(pin)
    }
}
