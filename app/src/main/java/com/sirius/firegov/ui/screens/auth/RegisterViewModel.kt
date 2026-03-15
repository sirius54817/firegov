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
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state = _state.asStateFlow()

    fun register(name: String, email: String, password: String, phoneNumber: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
            _state.value = AuthState.Error("Please fill all fields")
            return
        }

        if (phoneNumber.length < 10) {
            _state.value = AuthState.Error("Please enter a valid phone number")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Registration failed")
                authRepository.saveUser(uid, name, email, phoneNumber)
                _state.value = AuthState.Success
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    fun resetState() {
        _state.value = AuthState.Idle
    }
}
