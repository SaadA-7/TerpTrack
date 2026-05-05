package com.umd.terptrack.model

data class LostItem(
    val id: String = "",
    val title: String= "",
    val description: String = "",
    val buildingName: String = "",
    val imageUrl: String = "",
    val conditionRating: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    // ADDED FOR PART 3 (Ceyhun)
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)