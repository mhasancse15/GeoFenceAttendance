package com.example.geoattendance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geoattendance.data.LocationRepository
import com.example.geoattendance.data.OfficeLocationStore
import com.example.geoattendance.util.DEFAULT_ATTENDANCE_WINDOW
import com.example.geoattendance.util.GeoPoint
import com.example.geoattendance.util.distanceBetween
import com.example.geoattendance.util.isWithinOfficeRadius
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceUiState(
    val officeLocation: GeoPoint? = null,
    val currentLocation: GeoPoint? = null,
    val distanceMeters: Float? = null,
    val isWithinRange: Boolean = false,
    val isSettingOffice: Boolean = false,
    val isTimeWindowOpen: Boolean = false,
    val attendanceMarked: Boolean = false,
    val error: String? = null
) {
    val canMarkAttendance: Boolean
        get() = isWithinRange && isTimeWindowOpen && !attendanceMarked && officeLocation != null

    val windowLabel: String get() = DEFAULT_ATTENDANCE_WINDOW.label()
}

class AttendanceViewModel(
    private val locationRepository: LocationRepository,
    private val officeLocationStore: OfficeLocationStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        // Reflect the persisted office location as soon as it's available,
        // and keep it in sync with the live GPS stream + range/time checks.
        viewModelScope.launch {
            combine(
                officeLocationStore.officeLocation,
                locationRepository.observeLocation()
            ) { office, current -> office to current }
                .collect { (office, current) ->
                    val distance = office?.let { distanceBetween(it, current) }
                    _uiState.update { state ->
                        state.copy(
                            officeLocation = office,
                            currentLocation = current,
                            distanceMeters = distance,
                            isWithinRange = distance?.isWithinOfficeRadius() ?: false,
                            isTimeWindowOpen = DEFAULT_ATTENDANCE_WINDOW.isOpenNow()
                        )
                    }
                }
        }
    }

    fun setOfficeLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSettingOffice = true, error = null) }
            try {
                val point = locationRepository.getCurrentLocation()
                if (point != null) {
                    officeLocationStore.save(point)
                } else {
                    _uiState.update { it.copy(error = "Couldn't get a GPS fix. Try again outdoors.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to set office location.") }
            } finally {
                _uiState.update { it.copy(isSettingOffice = false) }
            }
        }
    }

    fun markAttendance() {
        val state = _uiState.value
        if (!state.canMarkAttendance) return
        // TODO: call the backend attendance API here.
        _uiState.update { it.copy(attendanceMarked = true) }
    }
}
