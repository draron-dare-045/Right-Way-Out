package com.example.rightway_out.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rightway_out.domain.model.StudentModel
import com.example.rightway_out.domain.repository.ClearanceRepository
import com.example.rightway_out.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDashboardState(val student: StudentModel? = null, val isLoading: Boolean = false, val errorMessage: String? = null)
data class AdminScreenState(val students: List<StudentModel> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null)
data class UpdateState(val isUpdating: Boolean = false, val successMessage: String? = null, val errorMessage: String? = null)

@HiltViewModel
class ClearanceViewModel @Inject constructor(
    private val repository: ClearanceRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(StudentDashboardState())
    val dashboardState: StateFlow<StudentDashboardState> = _dashboardState.asStateFlow()

    private val _adminState = MutableStateFlow(AdminScreenState())
    val adminState: StateFlow<AdminScreenState> = _adminState.asStateFlow()

    private val _updateState = MutableStateFlow(UpdateState())
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun loadStudentDashboard(studentId: String) {
        viewModelScope.launch {
            repository.getStudentClearance(studentId).collect { resource ->
                _dashboardState.update {
                    when (resource) {
                        is Resource.Loading -> it.copy(isLoading = true, errorMessage = null)
                        is Resource.Success -> it.copy(isLoading = false, student = resource.data, errorMessage = null)
                        is Resource.Error   -> it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }

    fun loadAllStudents() {
        viewModelScope.launch {
            repository.getAllStudents().collect { resource ->
                _adminState.update {
                    when (resource) {
                        is Resource.Loading -> it.copy(isLoading = true, errorMessage = null)
                        is Resource.Success -> it.copy(isLoading = false, students = resource.data, errorMessage = null)
                        is Resource.Error   -> it.copy(isLoading = false, errorMessage = resource.message)
                    }
                }
            }
        }
    }

    fun updateClearance(studentId: String, department: String, status: String, comment: String) {
        viewModelScope.launch {
            _updateState.update { it.copy(isUpdating = true, successMessage = null, errorMessage = null) }
            val result = repository.updateClearanceStatus(studentId, department, status, comment)
            _updateState.update {
                when (result) {
                    is Resource.Success -> it.copy(isUpdating = false, successMessage = "Clearance updated successfully.")
                    is Resource.Error   -> it.copy(isUpdating = false, errorMessage = result.message)
                    is Resource.Loading -> it
                }
            }
        }
    }

    fun clearUpdateFeedback() = _updateState.update { it.copy(successMessage = null, errorMessage = null) }
    val currentUserId get() = auth.currentUser?.uid

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearLocalCache()
            auth.signOut()
            onComplete()
        }
    }
}
