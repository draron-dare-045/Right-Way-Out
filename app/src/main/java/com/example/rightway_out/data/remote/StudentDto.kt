package com.example.rightway_out.data.remote

import com.example.rightway_out.data.local.StudentEntity

data class StudentDto(
    val name: String = "",
    val admissionNumber: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val libraryStatus: String = "PENDING",
    val boardingStatus: String = "PENDING",
    val sportsStatus: String = "PENDING",
    val financeStatus: String = "PENDING",
    val libraryComment: String = "",
    val boardingComment: String = "",
    val sportsComment: String = "",
    val financeComment: String = ""
) {
    fun toEntity(id: String): StudentEntity = StudentEntity(
        id = id, name = name, admissionNumber = admissionNumber,
        email = email, role = role,
        libraryStatus = libraryStatus, boardingStatus = boardingStatus,
        sportsStatus = sportsStatus, financeStatus = financeStatus,
        libraryComment = libraryComment, boardingComment = boardingComment,
        sportsComment = sportsComment, financeComment = financeComment,
        lastSyncedAt = System.currentTimeMillis()
    )
}
