package com.example.rightway_out

data class Student(
    val id: Int = 0,
    val admissionNumber: String,
    val name: String,
    val form: String,
    var libraryCleared: Boolean = false,
    var accountsCleared: Boolean = false,
    var sportsCleared: Boolean = false
) {
    fun isFullyCleared() = libraryCleared && accountsCleared && sportsCleared
}