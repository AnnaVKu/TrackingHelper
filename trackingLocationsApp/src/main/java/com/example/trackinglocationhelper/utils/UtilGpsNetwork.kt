package com.example.trackinglocationhelper.utils

import android.annotation.SuppressLint
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

class UtilGpsNetwork {
    companion object {
        fun isGpsEnabled(locationManager: LocationManager): Boolean {
            locationManager?.let {
                try {
                    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return false
        }

        @SuppressLint("MissingPermission")
        fun isNetworkEnabled(connectivityManager: ConnectivityManager): Boolean {
            connectivityManager?.let {
                try {
                    val networkInfo: Network? = connectivityManager.activeNetwork ?: return false
                    val actNet = connectivityManager.getNetworkCapabilities(networkInfo) ?: return false
                    return when {
                        actNet.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        else -> false
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            return false
        }
    }
}