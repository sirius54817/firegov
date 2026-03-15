package com.sirius.firegov.ui.screens.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirius.firegov.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _navigationState = MutableStateFlow<SplashNavigation?>(null)
    val navigationState = _navigationState.asStateFlow()

    init {
        Log.d("SplashViewModel", "init: Checking auth state")
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUserUid
                Log.d("SplashViewModel", "checkAuthState: uid = $uid")
                if (uid == null) {
                    Log.d("SplashViewModel", "checkAuthState: navigating to login")
                    _navigationState.value = SplashNavigation.ToLogin
                } else {
                    Log.d("SplashViewModel", "checkAuthState: fetching user profile")
                    val user = authRepository.getUser(uid)
                    Log.d("SplashViewModel", "checkAuthState: user = $user")
                    
                    if (user?.role == "citizen") {
                        if (user.name.isBlank() || user.phoneNumber.isBlank()) {
                            Log.d("SplashViewModel", "checkAuthState: profile incomplete, navigating to home (profile tab will be forced)")
                            _navigationState.value = SplashNavigation.ToHome
                        } else {
                            _navigationState.value = SplashNavigation.ToHome
                        }
                    } else {
                        _navigationState.value = SplashNavigation.ToLogin
                    }
                }
            } catch (e: Exception) {
                Log.e("SplashViewModel", "checkAuthState error", e)
                _navigationState.value = SplashNavigation.ToLogin
            }
        }
    }
}

sealed class SplashNavigation {
    object ToLogin : SplashNavigation()
    object ToHome : SplashNavigation()
}
