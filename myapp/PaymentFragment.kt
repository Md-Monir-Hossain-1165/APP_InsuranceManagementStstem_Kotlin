package com.example.myapp

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PaymentFragment : Fragment(R.layout.fragment_payment) {


    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Views
        val spinnerPolicy: Spinner = view.findViewById(R.id.spinnerPolicy)
        val spinnerPaymentMethod: Spinner = view.findViewById(R.id.spinnerPaymentMethod)
        val paymentDetailsContainer: LinearLayout = view.findViewById(R.id.paymentDetailsContainer)
        val btnSubmit: Button = view.findViewById(R.id.btnSubmit)
        val etPremiumAmount: EditText = view.findViewById(R.id.etPremiumAmount)

        val tvPolicyHeader: TextView = view.findViewById(R.id.tvPolicyHeader)
        val llPolicyDetails: LinearLayout = view.findViewById(R.id.llPolicyDetails)

        // Initialize Firebase Auth and check user authentication
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

        // Set up Spinner for payment methods
        val paymentMethodsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.payment_methods,
            android.R.layout.simple_spinner_item
        )
        paymentMethodsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaymentMethod.adapter = paymentMethodsAdapter

        // Set up Spinner for policy options
        val policyOptionsAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.policy_options,
            android.R.layout.simple_spinner_item
        )
        policyOptionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPolicy.adapter = policyOptionsAdapter

        // Handle policy selection
        spinnerPolicy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedPolicy = parent?.getItemAtPosition(position).toString()
                if (selectedPolicy.isNotEmpty()) {
                    displayPolicyDetails(selectedPolicy, llPolicyDetails, tvPolicyHeader)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Handle payment method selection
        spinnerPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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
            val amount = etPremiumAmount.text.toString().toDoubleOrNull()
            val paymentMethod = spinnerPaymentMethod.selectedItem?.toString() ?: "N/A"

            // Validate the required fields
            if (amount == null || paymentMethod == "Select Payment Method" || policy == "N/A") {
                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }

            // Validate payment method-specific fields
            when (paymentMethod) {
                "Credit Card" -> if (!isCreditCardFieldsValid()) {
                    Toast.makeText(
                        context,
                        "Please fill in all credit card fields",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                "Net Banking" -> if (!isNetBankingFieldsValid()) {
                    Toast.makeText(
                        context,
                        "Please fill in all net banking fields",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                "Mobile Banking" -> if (!isMobileBankingFieldsValid()) {
                    Toast.makeText(
                        context,
                        "Please fill in all mobile banking fields",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }

            // Retrieve current payment data
            val policyRef = database.child(policy)
            policyRef.get().addOnSuccessListener { snapshot ->
                val currentAmount = snapshot.child("amount").getValue(Double::class.java) ?: 0.0
                val updatedAmount = currentAmount + amount

                // Prepare updated payment data
                val paymentData = mapOf(
                    "amount" to updatedAmount,
                    "paymentMethod" to paymentMethod
                )

                // Update the payment data in the database
                policyRef.setValue(paymentData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "Payment submitted successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                        displayPolicyDetails(policy, llPolicyDetails, tvPolicyHeader)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to submit payment: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Error retrieving payment info: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
        // Validate Credit Card fields
    private fun isCreditCardFieldsValid(): Boolean {
        val container = view?.findViewById<LinearLayout>(R.id.paymentDetailsContainer)
        val cardNumber = container?.findViewWithTag<EditText>("cardNumber")
        val expiryDate = container?.findViewWithTag<EditText>("expiryDate")
        val cvv = container?.findViewWithTag<EditText>("cvv")
        return !(cardNumber?.text.isNullOrEmpty() || expiryDate?.text.isNullOrEmpty() || cvv?.text.isNullOrEmpty())
    }

    // Validate Net Banking fields
    private fun isNetBankingFieldsValid(): Boolean {
        val container = view?.findViewById<LinearLayout>(R.id.paymentDetailsContainer)
        val bankName = container?.findViewWithTag<EditText>("bankName")
        val password = container?.findViewWithTag<EditText>("password")
        return !(bankName?.text.isNullOrEmpty() || password?.text.isNullOrEmpty())
    }

    // Validate Mobile Banking fields
    private fun isMobileBankingFieldsValid(): Boolean {
        val container = view?.findViewById<LinearLayout>(R.id.paymentDetailsContainer)
        val provider = container?.findViewWithTag<Spinner>("provider")
        val mobileNumber = container?.findViewWithTag<EditText>("mobileNumber")
        val pin = container?.findViewWithTag<EditText>("pin")
        return !(provider == null || mobileNumber?.text.isNullOrEmpty() || pin?.text.isNullOrEmpty())
    }

    // Add Credit Card fields dynamically
    private fun addCreditCardFields(container: LinearLayout) {
        val cardNumber = EditText(context).apply {
            hint = "Card Number"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "cardNumber"
        }
        val expiryDate = EditText(context).apply {
            hint = "Expiry Date (MM/YY)"
            inputType = android.text.InputType.TYPE_CLASS_DATETIME
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "expiryDate"
        }
        val cvv = EditText(context).apply {
            hint = "CVV"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "cvv"
        }
        container.addView(cardNumber)
        container.addView(expiryDate)
        container.addView(cvv)
    }

    // Add Net Banking fields dynamically
    private fun addNetBankingFields(container: LinearLayout) {
        val bankName = EditText(context).apply {
            hint = "Bank Name"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "bankName"
        }
        val password = EditText(context).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "password"
        }
        container.addView(bankName)
        container.addView(password)
    }

    // Add Mobile Banking fields dynamically
    private fun addMobileBankingFields(container: LinearLayout) {
        val provider = Spinner(context).apply {
            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listOf("Select Provider","Bkash","","Nagad","")
            )
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(10, 20, 10, 20) }
            tag = "provider"
        }
        val mobileNumber = EditText(context).apply {
            hint = "Mobile Number"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "mobileNumber"
        }
        val pin = EditText(context).apply {
            hint = "PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(8, 8, 8, 8) }
            tag = "pin"
        }
        container.addView(provider)
        container.addView(mobileNumber)
        container.addView(pin)
    }
    private fun displayPolicyDetails(policy: String, container: LinearLayout, header: TextView) {
        database.child(policy).get().addOnSuccessListener { snapshot ->
            container.removeAllViews()
            if (snapshot.exists()) {
                header.visibility = View.VISIBLE
                val amount = snapshot.child("amount").getValue(Double::class.java) ?: 0.0
                val textView = TextView(context).apply {
                    text = "$policy: $amount BDT"
                    textSize = 14f
                    setPadding(8, 8, 8, 8)
                }
                container.addView(textView)
            } else {
                header.visibility = View.GONE
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error fetching details: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }
}
