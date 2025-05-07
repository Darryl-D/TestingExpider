package com.example.testingexpider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.widget.EditText
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AddFragment : Fragment() {

    private lateinit var numberPickerMonth: NumberPicker
    private lateinit var numberPickerDate: NumberPicker
    private lateinit var numberPickerYear: NumberPicker
    private lateinit var foodNameEditText: EditText
    private lateinit var addItemButton: Button
    private lateinit var foodTypeSpinner: Spinner

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)

        numberPickerMonth = view.findViewById(R.id.numberPickerMonth)
        numberPickerDate = view.findViewById(R.id.numberPickerDate)
        numberPickerYear = view.findViewById(R.id.numberPickerYear)
        foodNameEditText = view.findViewById(R.id.foodNameEditText)
        addItemButton = view.findViewById(R.id.addItemButton)
        foodTypeSpinner = view.findViewById(R.id.foodTypeSpinner)

        val foodTypes = listOf("Select Category", "Foods", "Drinks")
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            foodTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // Disable "Select food type"
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                if (position == 0) {
                    textView.setTextColor(Color.GRAY) // Hint-like color
                } else {
                    textView.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        foodTypeSpinner.adapter = adapter
        foodTypeSpinner.setSelection(0)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupNumberPickers()

        addItemButton.setOnClickListener {
            saveDateToFirestore()
        }

        return view
    }

    private fun setupNumberPickers() {
        numberPickerMonth.minValue = 1
        numberPickerMonth.maxValue = 12
        numberPickerMonth.wrapSelectorWheel = true

        numberPickerYear.minValue = 2000
        numberPickerYear.maxValue = 2100
        numberPickerYear.value = Calendar.getInstance().get(Calendar.YEAR)

        updateDaysInMonth()

        numberPickerMonth.setOnValueChangedListener { _, _, _ ->
            updateDaysInMonth()
        }

        numberPickerYear.setOnValueChangedListener { _, _, _ ->
            updateDaysInMonth()
        }
    }

    private fun updateDaysInMonth() {
        val month = numberPickerMonth.value
        val year = numberPickerYear.value

        val daysInMonth = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }

        numberPickerDate.minValue = 1
        numberPickerDate.maxValue = daysInMonth
        numberPickerDate.wrapSelectorWheel = true
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun saveDateToFirestore() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val foodName = foodNameEditText.text.toString().trim()
        val foodType = foodTypeSpinner.selectedItem.toString()

        if (foodName.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a product name", Toast.LENGTH_SHORT).show()
            return
        }

        if (foodType == "Select Category") {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val collectionName = when (foodType) {
            "Foods" -> "Foods"
            "Drinks" -> "Drinks"
            else -> {
                Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val dateData = hashMapOf(
            "item_Type" to foodType,
            "item_Name" to foodName,
            "date_Day" to numberPickerDate.value,
            "date_Month" to numberPickerMonth.value,
            "date_Year" to numberPickerYear.value,
        )

        db.collection("users")
            .document(user.uid)
            .collection(collectionName)
            .add(dateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "$collectionName Saved!", Toast.LENGTH_SHORT).show()
                foodNameEditText.text.clear()
                foodTypeSpinner.setSelection(0)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
