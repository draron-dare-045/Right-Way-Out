package com.example.rightway_out.domain.repository

import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.util.Resource
import kotlinx.coroutines.flow.Flow

interface ClearanceRepository {
    fun getStudentClearance(studentId: String): Flow<Resource<StudentModel>>
    fun getAllStudents(): Flow<Resource<List<StudentModel>>>
    suspend fun updateClearanceStatus(studentId: String, department: String, status: String, comment: String): Resource<Unit>
    suspend fun syncStudent(studentId: String): Resource<Unit>
    suspend fun clearLocalCache()
}
