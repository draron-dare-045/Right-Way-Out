package com.example.rightway_out.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingItemDao {

    @Query("SELECT * FROM shopping_items WHERE studentId = :studentId ORDER BY isPurchased ASC, createdAt DESC")
    fun getItemsForStudent(studentId: String): Flow<List<ShoppingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: ShoppingItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<ShoppingItemEntity>)

    @Query("UPDATE shopping_items SET isPurchased = :isPurchased WHERE id = :itemId")
    suspend fun togglePurchased(itemId: String, isPurchased: Boolean)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: String)

    @Query("DELETE FROM shopping_items WHERE studentId = :studentId")
    suspend fun clearStudentItems(studentId: String)

    @Query("SELECT SUM(estimatedCost * quantity) FROM shopping_items WHERE studentId = :studentId AND isPurchased = 0")
    fun getTotalCostForStudent(studentId: String): Flow<Double?>
}
