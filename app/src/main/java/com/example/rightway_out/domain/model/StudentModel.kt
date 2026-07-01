package com.example.rightway_out.domain.model

import com.example.rightway_out.data.local.StudentEntity

data class StudentModel(
    val id: String,
    val name: String,
    val admissionNumber: String,
    val email: String,
    val role: String,
    val libraryStatus: ClearanceStatus,
    val boardingStatus: ClearanceStatus,
    val sportsStatus: ClearanceStatus,
    val financeStatus: ClearanceStatus,
    val libraryComment: String,
    val boardingComment: String,
    val sportsComment: String,
    val financeComment: String
) {
    val isFullyCleared get() = listOf(libraryStatus, boardingStatus, sportsStatus, financeStatus)
        .all { it == ClearanceStatus.CLEARED }

    val hasFlaggedDepartment get() = listOf(libraryStatus, boardingStatus, sportsStatus, financeStatus)
        .any { it == ClearanceStatus.FLAGGED }
}

enum class ClearanceStatus {
    PENDING, CLEARED, FLAGGED;
    companion object {
        fun from(value: String) = when (value.uppercase()) {
            "CLEARED" -> CLEARED
            "FLAGGED" -> FLAGGED
            else      -> PENDING
        }
    }
}

fun StudentEntity.toDomain() = StudentModel(
    id = id, name = name, admissionNumber = admissionNumber,
    email = email, role = role,
    libraryStatus = ClearanceStatus.from(libraryStatus),
    boardingStatus = ClearanceStatus.from(boardingStatus),
    sportsStatus = ClearanceStatus.from(sportsStatus),
    financeStatus = ClearanceStatus.from(financeStatus),
    libraryComment = libraryComment, boardingComment = boardingComment,
    sportsComment = sportsComment, financeComment = financeComment
)
