package com.example.rightway_out.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StudentEntity::class, ShoppingItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class StudentDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun shoppingItemDao(): ShoppingItemDao
}
