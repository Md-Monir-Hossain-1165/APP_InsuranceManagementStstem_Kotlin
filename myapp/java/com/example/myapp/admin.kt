package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class admin : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var lvUserPayments: ListView
    private lateinit var tvAdminHeader: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Views
        lvUserPayments = findViewById(R.id.lvUserPayments)
        tvAdminHeader = findViewById(R.id.tvAdminHeader)
        val Button: Button = findViewById(R.id.tvAdminHeader)
        Button.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("users")

        // Fetch data from Firebase
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataList = mutableListOf<Map<String, String>>()
                var serialNumber = 1

                for (userSnapshot in snapshot.children) {
                    val email = userSnapshot.key?.replace("_", ".") ?: "Unknown User"
                    val paymentsSnapshot = userSnapshot.child("payments")

                    // Check if the user has any policies
                    if (paymentsSnapshot.hasChildren()) {
                        // Combine email and policies
                        val detailsBuilder = StringBuilder(email)
                        for (policySnapshot in paymentsSnapshot.children) {
                            val policy = policySnapshot.key ?: "Unknown Policy"
                            val amount = policySnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                            detailsBuilder.append("\n$policy: $amount BDT")
                        }

                        // Add data to the list
                        val userMap = mutableMapOf<String, String>()
                        userMap["serialNumber"] = "$serialNumber"
                        userMap["details"] = detailsBuilder.toString()
                        dataList.add(userMap)

                        serialNumber++
                    }
                }

                // Handle case where no data is available
                if (dataList.isEmpty()) {
                    val emptyMap = mutableMapOf<String, String>()
                    emptyMap["serialNumber"] = ""
                    emptyMap["details"] = "No data available."
                    dataList.add(emptyMap)
                }

                // Set up the custom adapter
                val adapter = object : BaseAdapter() {
                    override fun getCount(): Int = dataList.size
                    override fun getItem(position: Int): Map<String, String> = dataList[position]
                    override fun getItemId(position: Int): Long = position.toLong()
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(this@admin)
                            .inflate(R.layout.user_details_item, parent, false)

                        val serialNumberTextView: TextView = view.findViewById(R.id.serialNumber)
                        val detailsTextView: TextView = view.findViewById(R.id.detailsText)

                        val item = getItem(position)

                        serialNumberTextView.text = item["serialNumber"]
                        detailsTextView.text = item["details"]

                        return view
                    }
                }

                lvUserPayments.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@admin,
                    "Failed to fetch data: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }



    // ViewHolder class to hold references to views for better performance
private class ViewHolder(view: View) {
    val serialNumberTextView: TextView = view.findViewById(R.id.serialNumber)
    val emailTextView: TextView = view.findViewById(R.id.detailsText)
}}
