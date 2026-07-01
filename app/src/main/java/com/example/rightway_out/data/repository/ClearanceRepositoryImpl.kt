package com.example.rightway_out.data.repository

import android.util.Log
import com.example.rightway_out.data.local.StudentDao
import com.example.rightway_out.data.remote.StudentDto
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.domain.model.toDomain
import com.example.rightway_out.domain.repository.ClearanceRepository
import com.example.rightway_out.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "ClearanceRepo"
private const val COLLECTION = "students"

@Singleton
class ClearanceRepositoryImpl @Inject constructor(
    private val dao: StudentDao,
    private val firestore: FirebaseFirestore
) : ClearanceRepository {

    override fun getStudentClearance(studentId: String): Flow<Resource<StudentModel>> = flow {
        emit(Resource.Loading)
        val cached = dao.getStudentByIdOnce(studentId)
        if (cached != null) emit(Resource.Success(cached.toDomain()))
        try {
            val snapshot = firestore.collection(COLLECTION).document(studentId).get().await()
            val dto = snapshot.toObject(StudentDto::class.java)
            if (dto != null) {
                val entity = dto.toEntity(studentId)
                dao.upsertStudent(entity)
                emit(Resource.Success(entity.toDomain()))
            } else if (cached == null) {
                emit(Resource.Error("Student record not found."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore fetch failed for $studentId", e)
            if (cached == null) emit(Resource.Error("Could not load data. Check your connection.", e))
        }
    }

    override fun getAllStudents(): Flow<Resource<List<StudentModel>>> = channelFlow {
        send(Resource.Loading)
        dao.getAllStudents()
            .map { entities -> Resource.Success(entities.map { it.toDomain() }) }
            .collect { send(it) }
        awaitClose()
    }.onStart { syncAllStudentsFromFirestore() }

    private suspend fun syncAllStudentsFromFirestore() {
        try {
            val snapshot = firestore.collection(COLLECTION).get().await()
            val entities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(StudentDto::class.java)?.toEntity(doc.id)
            }
            dao.upsertStudents(entities)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync all students", e)
        }
    }

    override suspend fun updateClearanceStatus(
        studentId: String,
        department: String,
        status: String,
        comment: String
    ): Resource<Unit> {
        return try {
            // Map department name to exact Firestore field names
            val statusField = when (department.lowercase()) {
                "library"  -> "libraryStatus"
                "boarding" -> "boardingStatus"
                "sports"   -> "sportsStatus"
                "finance"  -> "financeStatus"
                else       -> "${department.lowercase()}Status"
            }
            val commentField = when (department.lowercase()) {
                "library"  -> "libraryComment"
                "boarding" -> "boardingComment"
                "sports"   -> "sportsComment"
                "finance"  -> "financeComment"
                else       -> "${department.lowercase()}Comment"
            }

            val updates = mapOf(
                statusField  to status,
                commentField to comment
            )

            Log.d(TAG, "Updating $studentId: $updates")

            firestore.collection(COLLECTION)
                .document(studentId)
                .update(updates)
                .await()

            // Sync the updated document back to Room
            syncStudent(studentId)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update clearance for $studentId", e)
            Resource.Error("Update failed: ${e.localizedMessage}", e)
        }
    }

    override suspend fun syncStudent(studentId: String): Resource<Unit> {
        return try {
            val snapshot = firestore.collection(COLLECTION)
                .document(studentId).get().await()
            val dto = snapshot.toObject(StudentDto::class.java)
                ?: return Resource.Error("Student not found in Firestore.")
            dao.upsertStudent(dto.toEntity(studentId))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Sync failed: ${e.localizedMessage}", e)
        }
    }

    override suspend fun clearLocalCache() = dao.clearAllStudents()
}
