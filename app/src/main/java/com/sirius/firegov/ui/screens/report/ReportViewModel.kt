package com.sirius.firegov.ui.screens.report

import android.location.Geocoder
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.sirius.firegov.data.model.Incident
import com.sirius.firegov.data.repository.AuthRepository
import com.sirius.firegov.data.repository.FireStationRepository
import com.sirius.firegov.data.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.util.Log

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val incidentRepository: IncidentRepository,
    private val stationRepository: FireStationRepository,
    private val geocoder: Geocoder
) : ViewModel() {

    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state = _state.asStateFlow()

    private val _location = MutableStateFlow<GeoPoint?>(null)
    val location = _location.asStateFlow()

    private val _address = MutableStateFlow("")
    val address = _address.asStateFlow()

    private val _photos = MutableStateFlow<List<Uri>>(emptyList())
    val photos = _photos.asStateFlow()

    fun updateLocation(lat: Double, lng: Double) {
        val geoPoint = GeoPoint(lat, lng)
        _location.value = geoPoint
        Log.d("ReportViewModel", "updateLocation: $lat, $lng")
        viewModelScope.launch {
            try {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    _address.value = addresses[0].getAddressLine(0)
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "geocode error", e)
            }
        }
    }

    fun setAddress(address: String) {
        _address.value = address
    }

    fun addPhoto(uri: Uri) {
        if (_photos.value.size < 3) {
            _photos.value += uri
        }
    }

    fun submitReport(description: String) {
        val loc = _location.value
        val addr = _address.value
        val uid = authRepository.currentUserUid

        Log.d("ReportViewModel", "submitReport: desc=$description, loc=$loc, uid=$uid")

        if (loc == null || addr.isBlank() || description.isBlank() || uid == null) {
            _state.value = ReportState.Error("Please fill all fields and allow location access.")
            return
        }

        viewModelScope.launch {
            _state.value = ReportState.Loading
            try {
                val photoUrls = if (_photos.value.isNotEmpty()) {
                    incidentRepository.uploadPhotos(uid, _photos.value)
                } else emptyList()

                Log.d("ReportViewModel", "submitReport: searching nearest station")
                val (nearestStation, distance) = stationRepository.findNearestStation(loc)
                Log.d("ReportViewModel", "submitReport: nearest=${nearestStation?.name}, dist=$distance")
                
                val user = authRepository.currentUser
                val incident = Incident(
                    reportedBy = uid,
                    reporterName = user?.displayName ?: "User",
                    reporterEmail = user?.email ?: "",
                    location = loc,
                    address = addr,
                    photoUrls = photoUrls,
                    description = description,
                    status = "pending",
                    assignedStation = nearestStation?.stationId,
                    nearestStationDistance = distance?.toDouble()
                )

                val success = incidentRepository.submitIncident(incident)
                if (success) {
                    Log.d("ReportViewModel", "submitReport: success")
                    _state.value = ReportState.Success
                } else {
                    Log.e("ReportViewModel", "submitReport: repository failure")
                    _state.value = ReportState.Error("Failed to submit report. Please try again.")
                }
            } catch (e: Exception) {
                Log.e("ReportViewModel", "submitReport: exception", e)
                _state.value = ReportState.Error(e.localizedMessage ?: "Submission failed.")
            }
        }
    }

    fun resetState() {
        _state.value = ReportState.Idle
        _photos.value = emptyList()
    }
}

sealed class ReportState {
    object Idle : ReportState()
    object Loading : ReportState()
    object Success : ReportState()
    data class Error(val message: String) : ReportState()
}
