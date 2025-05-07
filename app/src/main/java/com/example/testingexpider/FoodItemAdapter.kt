package com.example.testingexpider

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class FoodItemAdapter(private val items: List<FoodItem>) :
    RecyclerView.Adapter<FoodItemAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.itemNameText)
        val dateTextView: TextView = itemView.findViewById(R.id.expiryDateText)
        val countdownText: TextView = itemView.findViewById(R.id.countdownText) // New countdown TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.nameTextView.text = item.name
        holder.dateTextView.text = "${item.year}-${item.month}-${item.day}"

        val today = Calendar.getInstance()
        val expiry = Calendar.getInstance().apply {
            set(item.year, item.month - 1, item.day) // month is 0-based
        }

        val diffInMillis = expiry.timeInMillis - today.timeInMillis
        val daysLeft = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()

        // Check if the item expires in 7 days or less
        if (daysLeft <= 7) {
            holder.itemView.setBackgroundResource(R.drawable.item_list_rounded_background_orange) // Orange background for expiring items
            holder.countdownText.visibility = View.VISIBLE // Show countdown text

            // Set countdown text: "0 Days Left" if expired or today, otherwise show remaining days
            holder.countdownText.text = when {
                daysLeft <= 0 -> "0 Days Left"
                else -> "$daysLeft Days Left"
            }
        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_list_rounded_background) // Regular background for other items
            holder.countdownText.visibility = View.GONE // Hide countdown text
        }
    }

    override fun getItemCount(): Int = items.size
}
