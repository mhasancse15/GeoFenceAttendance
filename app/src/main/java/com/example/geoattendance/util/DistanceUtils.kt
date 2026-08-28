package com.example.geoattendance.util

import android.location.Location

/** Simple immutable coordinate pair used across the app. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Radius within which a user is considered "at the office". */
const val OFFICE_RADIUS_METERS = 50f

/**
 * Distance in meters between two points, using the platform's
 * Location.distanceBetween (Vincenty-derived, accurate for short ranges).
 */
fun distanceBetween(from: GeoPoint, to: GeoPoint): Float {
    val results = FloatArray(1)
    Location.distanceBetween(
        from.latitude, from.longitude,
        to.latitude, to.longitude,
        results
    )
    return results[0]
}

fun Float.isWithinOfficeRadius(): Boolean = this <= OFFICE_RADIUS_METERS
