package com.example.trackinglocationhelper.utils

import android.location.Location

class UtilLocationIntoString {
    companion object {
        fun convertLocationIntoString(location: Location): String? =
            location.time.toString() + "," + Location.convert(
                location.latitude,
                Location.FORMAT_DEGREES
            ) + "," + Location.convert(location.longitude, Location.FORMAT_DEGREES)
    }
}