package com.example.testingexpider

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var btnFoods: Button
    private lateinit var btnDrinks: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FoodItemAdapter
    private val itemList = mutableListOf<FoodItem>()
    private lateinit var topItemsContainer: LinearLayout
    private lateinit var topNoItemsText: TextView
    private lateinit var bottomNoItemsText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize UI elements
        btnFoods = view.findViewById(R.id.btnFoods)
        btnDrinks = view.findViewById(R.id.btnDrinks)
        recyclerView = view.findViewById(R.id.itemRecyclerView)
        topItemsContainer = view.findViewById(R.id.topItemsContainer)
        topNoItemsText = view.findViewById(R.id.topNoItemsText)
        bottomNoItemsText = view.findViewById(R.id.bottomNoItemsText)

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // RecyclerView setup
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FoodItemAdapter(itemList)
        recyclerView.adapter = adapter

        // Load top expiry items for horizontal scroll view
        loadTop5ExpiringItems()

        // Default: highlight Foods and load Foods list
        highlightSelectedButton(btnFoods, btnDrinks)
        loadItems("Foods")

        // Button click listeners
        btnFoods.setOnClickListener {
            highlightSelectedButton(btnFoods, btnDrinks)
            loadItems("Foods")
        }

        btnDrinks.setOnClickListener {
            highlightSelectedButton(btnDrinks, btnFoods)
            loadItems("Drinks")
        }

        return view
    }

    private fun highlightSelectedButton(selected: Button, unselected: Button) {
        val yellow = ContextCompat.getColor(requireContext(), R.color.yellow)
        val gray = ContextCompat.getColor(requireContext(), R.color.gray)

        selected.setBackgroundColor(yellow)
        selected.setTextColor(Color.WHITE)

        unselected.setBackgroundColor(gray)
        unselected.setTextColor(Color.BLACK)
    }

    private fun loadItems(type: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users")
            .document(user.uid)
            .collection(type)
            .get()
            .addOnSuccessListener { snapshot ->
                itemList.clear()
                for (doc in snapshot.documents) {
                    // Fetching the data
                    val name = doc.getString("item_Name") ?: continue
                    val day = doc.getLong("date_Day")?.toInt() ?: continue
                    val month = doc.getLong("date_Month")?.toInt() ?: continue
                    val year = doc.getLong("date_Year")?.toInt() ?: continue

                    // Add Log.d here to check if the data is fetched correctly
                    Log.d("HomeFragment", "Item Name: $name, Expiry Date: $day/$month/$year")

                    val foodItem = FoodItem(name, day, month, year)
                    itemList.add(foodItem)

                    // Schedule notification
                    val calendar = Calendar.getInstance().apply {
                        set(year, month - 1, day, 9, 0, 0) // 9 AM on the expiry date
                    }

                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                        scheduleNotification(
                            requireContext(),
                            "Expiring Soon: ${foodItem.name}",
                            "Your ${foodItem.name} expires today!",
                            calendar.timeInMillis
                        )
                    }
                }

                // Sorting the items
                itemList.sortWith(
                    compareBy<FoodItem> { it.year }
                        .thenBy { it.month }
                        .thenBy { it.day }
                )

                // Handle empty state
                if (itemList.isEmpty()) {
                    bottomNoItemsText.visibility = View.VISIBLE
                } else {
                    bottomNoItemsText.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading items: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun loadTop5ExpiringItems() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val foodItems = mutableListOf<FoodItem>()
        val drinkItems = mutableListOf<FoodItem>()

        val foodTask = db.collection("users")
            .document(user.uid)
            .collection("Foods")
            .get()

        val drinkTask = db.collection("users")
            .document(user.uid)
            .collection("Drinks")
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(foodTask, drinkTask)
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    for (doc in snapshot.documents) {
                        val name = doc.getString("item_Name") ?: continue
                        val day = doc.getLong("date_Day")?.toInt() ?: continue
                        val month = doc.getLong("date_Month")?.toInt() ?: continue
                        val year = doc.getLong("date_Year")?.toInt() ?: continue
                        val item = FoodItem(name, day, month, year)

                        if (snapshot == foodTask.result) {
                            foodItems.add(item)
                        } else {
                            drinkItems.add(item)
                        }
                    }
                }

                val allItems = (foodItems + drinkItems).sortedWith(
                    compareBy<FoodItem> { it.year }
                        .thenBy { it.month }
                        .thenBy { it.day }
                )

                val top5 = allItems.take(5)

                topItemsContainer.removeAllViews()
                if (top5.isEmpty()) {
                    topNoItemsText.visibility = View.VISIBLE
                } else {
                    topNoItemsText.visibility = View.GONE

                    for (item in top5) {
                        val cardView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_scroll_card, topItemsContainer, false)

                        cardView.findViewById<TextView>(R.id.itemNameText).text = item.name
                        cardView.findViewById<TextView>(R.id.expiryDateText).text =
                            "${item.year}-${item.month}-${item.day}"

                        // Countdown logic
                        val today = Calendar.getInstance()
                        val expiry = Calendar.getInstance().apply {
                            set(item.year, item.month - 1, item.day)
                        }
                        val diffInMillis = expiry.timeInMillis - today.timeInMillis
                        val daysLeft = (diffInMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

                        cardView.findViewById<TextView>(R.id.countdownText).text = "$daysLeft Days Left"

                        topItemsContainer.addView(cardView)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load top items: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
