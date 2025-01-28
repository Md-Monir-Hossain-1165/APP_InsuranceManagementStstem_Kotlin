package com.example.myapp

import android.os.Bundle

import android.view.View

import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ClaimFragment : Fragment(R.layout.fragment_claim) {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        val spinnerPolicy: Spinner = view.findViewById(R.id.spinnerPolicy)
        val etPremiumAmount: EditText = view.findViewById(R.id.etPremiumAmount)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmit)
        val tvClaimDetails: TextView = view.findViewById(R.id.tvClaimDetails)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(
                context,
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

        // Handle Submit button click
        btnSubmit.setOnClickListener {
            val policy = spinnerPolicy.selectedItem?.toString() ?: "N/A"
            val claimAmount = etPremiumAmount.text.toString().toDoubleOrNull()

            if (claimAmount == null || policy == "N/A") {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Get current payment data from Firebase
            val policyRef = database.child(policy)
            policyRef.get().addOnSuccessListener { snapshot ->
                val currentPaymentAmount = snapshot.child("amount").getValue(Double::class.java) ?: 0.0

                if (claimAmount <= currentPaymentAmount) {
                    // Proceed with the claim
                    val updatedPaymentAmount = currentPaymentAmount - claimAmount

                    // Prepare updated claim data
                    val claimData = mapOf(
                        "claimedAmount" to claimAmount,
                        "remainingAmount" to updatedPaymentAmount
                    )

                    // Update the payment amount in Firebase
                    policyRef.child("amount").setValue(updatedPaymentAmount)
                        .addOnSuccessListener {
                            // Show claim details
                            showClaimDetails(policy, claimAmount, updatedPaymentAmount)

                            Toast.makeText(context, "Claim processed successfully!", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                context,
                                "Failed to update payment: ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    // If claim amount is greater than payment amount
                    Toast.makeText(context, "Claim amount exceeds available payment amount!", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error retrieving payment info: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showClaimDetails(policy: String, claimAmount: Double, remainingAmount: Double) {
        val claimDetailsText = "Policy: $policy\nClaim Amount: $claimAmount\nRemaining Amount: $remainingAmount"
        val tvClaimDetails: TextView = view?.findViewById(R.id.tvClaimDetails) ?: return
        tvClaimDetails.text = claimDetailsText
        tvClaimDetails.visibility = View.VISIBLE
    }
}
