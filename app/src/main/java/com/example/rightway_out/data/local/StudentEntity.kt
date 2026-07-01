package com.example.rightway_out.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val admissionNumber: String,
    val email: String,
    val role: String = "STUDENT",
    val libraryStatus: String = "PENDING",
    val boardingStatus: String = "PENDING",
    val sportsStatus: String = "PENDING",
    val financeStatus: String = "PENDING",
    val libraryComment: String = "",
    val boardingComment: String = "",
    val sportsComment: String = "",
    val financeComment: String = "",
    val lastSyncedAt: Long = 0L
)
