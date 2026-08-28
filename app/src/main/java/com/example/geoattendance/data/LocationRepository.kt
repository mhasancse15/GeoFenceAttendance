package com.example.geoattendance.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.example.geoattendance.util.GeoPoint
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper around FusedLocationProviderClient. Callers are responsible
 * for checking/requesting the ACCESS_FINE_LOCATION permission before calling
 * into this class - it assumes permission is already granted.
 */
class LocationRepository(context: Context) {

    private val client: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /** One-shot high-accuracy fetch, used by "Set Office Location". */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoPoint? {
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()
        val location = client.getCurrentLocation(request, null).await() ?: return null
        return GeoPoint(location.latitude, location.longitude)
    }

    /** Continuous updates used to drive the real-time distance indicator. */
    @SuppressLint("MissingPermission")
    fun observeLocation(intervalMillis: Long = 3_000L): Flow<GeoPoint> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    trySend(GeoPoint(loc.latitude, loc.longitude))
                }
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }
}
