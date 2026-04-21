package com.umd.terptrack.model

data class LostItem(
    val id: String = "",
    val description: String = "",
    val buildingName: String = "",
    val imageUrl: String = "",
    val conditionRating: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis()
)