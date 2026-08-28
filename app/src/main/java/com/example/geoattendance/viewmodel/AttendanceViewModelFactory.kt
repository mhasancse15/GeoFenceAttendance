package com.example.geoattendance.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.geoattendance.data.LocationRepository
import com.example.geoattendance.data.OfficeLocationStore

class AttendanceViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AttendanceViewModel(
            locationRepository = LocationRepository(appContext),
            officeLocationStore = OfficeLocationStore(appContext)
        ) as T
    }
}
