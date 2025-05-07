package com.example.testingexpider

data class FoodItem(
    val name: String = "",
    val day: Int = 1,
    val month: Int = 1,
    val year: Int = 2000
) {
    fun getFormattedDate(): String = "$year-$month-$day"
}
