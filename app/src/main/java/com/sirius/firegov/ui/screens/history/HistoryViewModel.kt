package com.sirius.firegov.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sirius.firegov.data.model.Incident
import com.sirius.firegov.data.repository.AuthRepository
import com.sirius.firegov.data.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents = _incidents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchHistory()
    }

    fun fetchHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.currentUserUid?.let { uid ->
                _incidents.value = incidentRepository.getIncidentHistory(uid)
            }
            _isLoading.value = false
        }
    }
}
