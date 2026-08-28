package com.example.geoattendance.util

import java.time.LocalTime

data class CheckInWindow(val start: LocalTime, val end: LocalTime) {
    fun isOpenNow(now: LocalTime = LocalTime.now()): Boolean =
        !now.isBefore(start) && !now.isAfter(end)

    fun label(): String {
        val fmt = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
        return "Available ${start.format(fmt)} - ${end.format(fmt)}"
    }
}

val DEFAULT_ATTENDANCE_WINDOW = CheckInWindow(LocalTime.of(9, 0), LocalTime.of(10, 30))
