package com.sirius.firegov.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.sirius.firegov.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Login failed")
                val role = authRepository.getUserRole(uid)
                if (role == "citizen") {
                    _state.value = AuthState.Success
                } else {
                    auth.signOut()
                    _state.value = AuthState.Error("Access denied: Not a citizen account")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val uid = authRepository.signInWithGoogle(idToken) ?: throw Exception("Google sign-in failed")
                val role = authRepository.getUserRole(uid)
                if (role == null) {
                    // New user from Google, default to citizen
                    val user = auth.currentUser
                    authRepository.saveUser(
                        uid = uid,
                        name = user?.displayName ?: "Google User",
                        email = user?.email ?: "",
                        phoneNumber = user?.phoneNumber ?: ""
                    )
                    _state.value = AuthState.Success
                } else if (role == "citizen") {
                    _state.value = AuthState.Success
                } else {
                    auth.signOut()
                    _state.value = AuthState.Error("Access denied: Not a citizen account")
                }
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Google sign-in failed")
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
