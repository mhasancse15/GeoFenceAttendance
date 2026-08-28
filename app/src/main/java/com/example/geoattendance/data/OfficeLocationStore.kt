package com.example.geoattendance.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.geoattendance.util.GeoPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.officeDataStore by preferencesDataStore(name = "office_location")

/**
 * Persists the one office GeoPoint the user sets in the "Set Office Location"
 * step. Backed by DataStore so it survives process death without any
 * boilerplate SharedPreferences listener wiring.
 */
class OfficeLocationStore(private val context: Context) {

    private object Keys {
        val LAT = doublePreferencesKey("office_lat")
        val LON = doublePreferencesKey("office_lon")
    }

    val officeLocation: Flow<GeoPoint?> = context.officeDataStore.data.map { prefs ->
        val lat = prefs[Keys.LAT]
        val lon = prefs[Keys.LON]
        if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }

    suspend fun save(point: GeoPoint) {
        context.officeDataStore.edit { prefs ->
            prefs[Keys.LAT] = point.latitude
            prefs[Keys.LON] = point.longitude
        }
    }

    suspend fun clear() {
        context.officeDataStore.edit { it.clear() }
    }
}
