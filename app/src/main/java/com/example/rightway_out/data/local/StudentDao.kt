package com.example.rightway_out.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getStudentById(studentId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentByIdOnce(studentId: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudents(students: List<StudentEntity>)

    @Query("""
        UPDATE students SET
            libraryStatus = :libraryStatus,
            boardingStatus = :boardingStatus,
            sportsStatus = :sportsStatus,
            financeStatus = :financeStatus,
            libraryComment = :libraryComment,
            boardingComment = :boardingComment,
            sportsComment = :sportsComment,
            financeComment = :financeComment,
            lastSyncedAt = :lastSyncedAt
        WHERE id = :studentId
    """)
    suspend fun updateClearanceStatus(
        studentId: String,
        libraryStatus: String, boardingStatus: String,
        sportsStatus: String, financeStatus: String,
        libraryComment: String, boardingComment: String,
        sportsComment: String, financeComment: String,
        lastSyncedAt: Long
    )

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: String)

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()
}
