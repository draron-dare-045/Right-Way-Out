package com.example.rightway_out.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rightway_out.data.local.ShoppingItemDao
import com.example.rightway_out.data.local.ShoppingItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

data class ShoppingUiState(
    val items: List<ShoppingItemEntity> = emptyList(),
    val totalCost: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val dao: ShoppingItemDao,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(ShoppingUiState())
    val state: StateFlow<ShoppingUiState> = _state.asStateFlow()

    fun loadItems(studentId: String) {
        viewModelScope.launch {
            // Observe local Room DB
            dao.getItemsForStudent(studentId)
                .combine(dao.getTotalCostForStudent(studentId)) { items, total ->
                    ShoppingUiState(items = items, totalCost = total ?: 0.0)
                }
                .collect { _state.value = it }
        }
        // Sync from Firestore in background
        viewModelScope.launch { syncFromFirestore(studentId) }
    }

    private suspend fun syncFromFirestore(studentId: String) {
        try {
            val snapshot = firestore.collection("students")
                .document(studentId)
                .collection("shopping_list")
                .get().await()

            val entities = snapshot.documents.map { doc ->
                ShoppingItemEntity(
                    id           = doc.id,
                    studentId    = studentId,
                    name         = doc.getString("name") ?: "",
                    quantity     = (doc.getLong("quantity") ?: 1L).toInt(),
                    unit         = doc.getString("unit") ?: "pcs",
                    estimatedCost = doc.getDouble("estimatedCost") ?: 0.0,
                    isPurchased  = doc.getBoolean("isPurchased") ?: false,
                    category     = doc.getString("category") ?: "General",
                    notes        = doc.getString("notes") ?: "",
                    createdAt    = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
            dao.upsertItems(entities)
        } catch (e: Exception) {
            // Silently fail — Room data still shows
        }
    }

    fun addItem(
        studentId: String,
        name: String,
        quantity: Int,
        unit: String,
        estimatedCost: Double,
        category: String,
        notes: String
    ) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val item = ShoppingItemEntity(
                id = id, studentId = studentId, name = name,
                quantity = quantity, unit = unit,
                estimatedCost = estimatedCost, category = category,
                notes = notes, isPurchased = false
            )
            // Save to Room immediately
            dao.upsertItem(item)

            // Sync to Firestore
            try {
                val data = hashMapOf(
                    "name" to name, "quantity" to quantity, "unit" to unit,
                    "estimatedCost" to estimatedCost, "category" to category,
                    "notes" to notes, "isPurchased" to false,
                    "createdAt" to item.createdAt
                )
                firestore.collection("students").document(studentId)
                    .collection("shopping_list").document(id).set(data).await()
            } catch (e: Exception) { /* Room already saved it */ }
        }
    }

    fun togglePurchased(studentId: String, item: ShoppingItemEntity) {
        viewModelScope.launch {
            val newValue = !item.isPurchased
            dao.togglePurchased(item.id, newValue)
            try {
                firestore.collection("students").document(studentId)
                    .collection("shopping_list").document(item.id)
                    .update("isPurchased", newValue).await()
            } catch (e: Exception) { /* Room already updated */ }
        }
    }

    fun deleteItem(studentId: String, itemId: String) {
        viewModelScope.launch {
            dao.deleteItem(itemId)
            try {
                firestore.collection("students").document(studentId)
                    .collection("shopping_list").document(itemId).delete().await()
            } catch (e: Exception) { /* Room already deleted */ }
        }
    }
}
