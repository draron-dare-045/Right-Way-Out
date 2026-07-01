package com.example.rightway_out.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(private val auth: FirebaseAuth) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(isLoggedIn = auth.currentUser != null))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val currentUser get() = auth.currentUser

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(errorMessage = "Email and password cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.update { it.copy(isLoading = false, isLoggedIn = true) }
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage ?: "Login failed.") }
            }
        }
    }

    fun clearError() = _authState.update { it.copy(errorMessage = null) }
}
