package com.sirius.firegov.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirius.firegov.data.model.User
import com.sirius.firegov.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus = _updateStatus.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.currentUserUid?.let { uid ->
                _user.value = authRepository.getUser(uid)
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(name: String, phoneNumber: String) {
        if (name.isBlank() || phoneNumber.isBlank()) {
            _updateStatus.value = UpdateStatus.Error("Fields cannot be empty")
            return
        }

        viewModelScope.launch {
            _updateStatus.value = UpdateStatus.Loading
            val uid = authRepository.currentUserUid ?: return@launch
            val success = authRepository.updateProfile(uid, name, phoneNumber)
            if (success) {
                _updateStatus.value = UpdateStatus.Success
                fetchProfile()
            } else {
                _updateStatus.value = UpdateStatus.Error("Failed to update profile")
            }
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }
}

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Loading : UpdateStatus()
    object Success : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}
