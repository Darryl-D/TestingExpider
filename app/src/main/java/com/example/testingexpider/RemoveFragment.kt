package com.example.testingexpider

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RemoveFragment : Fragment() {

    private lateinit var foodTypeSpinner: Spinner
    private lateinit var itemNameSpinner: Spinner
    private lateinit var checkDataButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_remove, container, false)

        foodTypeSpinner = view.findViewById(R.id.foodTypeRemoveSpinner)
        itemNameSpinner = view.findViewById(R.id.itemNameSpinner)
        checkDataButton = view.findViewById(R.id.checkDataButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupFoodTypeSpinner()

        checkDataButton.setOnClickListener {
            deleteSelectedItem() // Call the delete function instead of checking
        }

        return view
    }

    private fun setupFoodTypeSpinner() {
        val foodTypes = listOf("Select Category", "Foods", "Drinks")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            foodTypes
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        foodTypeSpinner.adapter = adapter
        foodTypeSpinner.setSelection(0)

        // Load items when user selects a valid type
        foodTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedType = foodTypeSpinner.selectedItem.toString()
                if (selectedType != "Select Category") {
                    loadItemNames(selectedType)
                } else {
                    clearItemNameSpinner()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    data class ItemEntry(val name: String, val year: Int, val month: Int, val day: Int) {
        fun formatted(): String = "$name, $year-$month-$day"
    }

    private fun loadItemNames(selectedType: String) {
        val user = auth.currentUser ?: return

        db.collection("users")
            .document(user.uid)
            .collection(selectedType)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = mutableListOf<ItemEntry>()

                for (doc in snapshot.documents) {
                    val name = doc.getString("item_Name")
                    val month = doc.getLong("date_Month")?.toInt()
                    val day = doc.getLong("date_Day")?.toInt()
                    val year = doc.getLong("date_Year")?.toInt()

                    if (!name.isNullOrEmpty() && month != null && day != null && year != null) {
                        items.add(ItemEntry(name, year, month, day))
                    }
                }

                // Sort by name first, then by year, month, and day
                items.sortWith(compareBy<ItemEntry> { it.name }
                    .thenBy { it.year }
                    .thenBy { it.month }
                    .thenBy { it.day })

                val itemNames = mutableListOf("Select Product Name")
                itemNames.addAll(items.map { it.formatted() })

                if (items.isEmpty()) {
                    itemNames.add("No items found")
                }

                val adapter = object : ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    itemNames
                ) {
                    override fun isEnabled(position: Int): Boolean {
                        return position != 0 && itemNames[position] != "No items found"
                    }

                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        val textView = view as TextView
                        textView.setTextColor(
                            if (position == 0 || itemNames[position] == "No items found")
                                Color.GRAY else Color.BLACK
                        )
                        return view
                    }
                }

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                itemNameSpinner.adapter = adapter
                itemNameSpinner.setSelection(0)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading items: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun clearItemNameSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listOf("Select Product Name")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemNameSpinner.adapter = adapter
    }

    private fun deleteSelectedItem() {
        val user = auth.currentUser ?: return

        val foodType = foodTypeSpinner.selectedItem.toString()
        val selectedItem = itemNameSpinner.selectedItem.toString()

        if (foodType == "Select Category" || selectedItem == "Select Product Name" || selectedItem == "No items found") {
            Toast.makeText(requireContext(), "Please select a valid item", Toast.LENGTH_SHORT).show()
            return
        }

        val parts = selectedItem.split(", ")
        if (parts.size != 2) {
            Toast.makeText(requireContext(), "Invalid item format", Toast.LENGTH_SHORT).show()
            return
        }

        val itemName = parts[0]
        val dateParts = parts[1].split("-")
        if (dateParts.size != 3) {
            Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        val year = dateParts[0].toIntOrNull()
        val month = dateParts[1].toIntOrNull()
        val day = dateParts[2].toIntOrNull()

        if (year == null || month == null || day == null) {
            Toast.makeText(requireContext(), "Invalid date values", Toast.LENGTH_SHORT).show()
            return
        }

        val collectionRef = db.collection("users")
            .document(user.uid)
            .collection(foodType)

        collectionRef
            .whereEqualTo("item_Name", itemName)
            .whereEqualTo("date_Year", year)
            .whereEqualTo("date_Month", month)
            .whereEqualTo("date_Day", day)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        collectionRef.document(doc.id).delete()
                    }
                    Toast.makeText(requireContext(), "$foodType deleted", Toast.LENGTH_SHORT).show()
                    loadItemNames(foodType)
                    foodTypeSpinner.setSelection(0) // Reset to "Select food type"
                    itemNameSpinner.adapter = null// Refresh list
                } else {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

