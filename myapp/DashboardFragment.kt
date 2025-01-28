package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize the life button
        val lifeButton: Button = view.findViewById(R.id.button_life)
        val healthButton: Button = view.findViewById(R.id.button_health)
        val carButton: Button = view.findViewById(R.id.button_car)
        val homeButton: Button = view.findViewById(R.id.button_home_insurance)

        // Set up the click listener
        lifeButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating to Life")

            // Start the LifeActivity
            val intent = Intent(requireContext(), life::class.java)
            startActivity(intent)
        }
        // Set up the click listener
        healthButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating to Life")

            // Start the LifeActivity
            val intent = Intent(requireContext(), health::class.java)
            startActivity(intent)
        }
        // Set up the click listener
        carButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating to Life")

            // Start the LifeActivity
            val intent = Intent(requireContext(), car::class.java)
            startActivity(intent)
        }
        // Set up the click listener
        homeButton.setOnClickListener {
            // Log the button click
            Log.d("life", "Yes button clicked, navigating to Life")

            // Start the LifeActivity
            val intent = Intent(requireContext(), home::class.java)
            startActivity(intent)
        }

        return view
    }
}
