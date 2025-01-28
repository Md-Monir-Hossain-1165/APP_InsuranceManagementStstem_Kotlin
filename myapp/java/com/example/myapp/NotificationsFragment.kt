package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fullNameTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var llPolicyDetails: LinearLayout
    private lateinit var policyHeader: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Initialize TextViews
        fullNameTextView = view.findViewById(R.id.tv_fullname)
        genderTextView = view.findViewById(R.id.tv_gender)
        emailTextView = view.findViewById(R.id.tv_email)
        llPolicyDetails = view.findViewById(R.id.llPolicyDetails)
        policyHeader = view.findViewById(R.id.tvPolicyHeader)

        // Load user details and policy details
        loadUserDetails()

        return view
    }

    private fun loadUserDetails() {
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.reference.child("users").child(userId)

            // Load fullName
            userRef.child("fullName").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fullName = task.result?.value as? String ?: "User"
                    fullNameTextView.text = fullName
                }
            }

            // Load gender
            userRef.child("gender").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val gender = task.result?.value as? String ?: "Not specified"
                    genderTextView.text = "Gender : $gender"
                }
            }

            // Load email
            emailTextView.text = "Email : ${auth.currentUser?.email ?: "Not provided"}"

            // Display paid policy details
            displayPaidPolicies(currentUser.email ?: "")
        } else {
            fullNameTextView.text = "User"
            genderTextView.text = "Not specified"
            emailTextView.text = "Not provided"
        }
    }

    // Method to display paid policies for the user
    private fun displayPaidPolicies(email: String) {
        val userKey = email.replace(".", "_")
        val paymentDatabase = FirebaseDatabase.getInstance().getReference("users/$userKey/payments")

        paymentDatabase.get().addOnSuccessListener { snapshot ->
            llPolicyDetails.removeAllViews() // Remove any existing views in the container

            if (snapshot.exists()) {
                policyHeader.visibility = View.VISIBLE // Show the header when there are policies
                snapshot.children.forEach { policySnapshot ->
                    val policyName = policySnapshot.key ?: "Unknown Policy"
                    val amount = policySnapshot.child("amount").getValue(Double::class.java) ?: 0.0

                    // Create a TextView for each policy
                    val textView = TextView(context).apply {
                        text = "$policyName: $amount BDT"
                        textSize = 14f
                        setPadding(8, 8, 8, 8)
                    }
                    llPolicyDetails.addView(textView) // Add the new TextView to the container
                }
            } else {
                policyHeader.visibility = View.GONE // Hide the header if no policy details exist
            }
        }.addOnFailureListener {
            Toast.makeText(
                context,
                "Error fetching policy details: ${it.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
