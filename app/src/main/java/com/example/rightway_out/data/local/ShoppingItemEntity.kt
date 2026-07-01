package com.example.rightway_out.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: String,          // Firestore doc ID
    val studentId: String,
    val name: String,
    val quantity: Int = 1,
    val unit: String = "pcs",            // pcs, kg, litres, pairs, etc.
    val estimatedCost: Double = 0.0,
    val isPurchased: Boolean = false,
    val category: String = "General",    // Stationery, Uniform, Food, Toiletries, Bedding, General
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
