package com.sirius.firegov.ui.screens.incident

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.sirius.firegov.data.model.Incident
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class IncidentDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val incidentId: String? = savedStateHandle["incidentId"]

    private val _incident = MutableStateFlow<Incident?>(null)
    val incident = _incident.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchIncident()
    }

    private fun fetchIncident() {
        incidentId?.let { id ->
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val doc = firestore.collection("incidents").document(id).get().await()
                    _incident.value = doc.toObject(Incident::class.java)
                } catch (e: Exception) {
                    // Handle error
                }
                _isLoading.value = false
            }
        }
    }
}
