package com.lianshan.lslife.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeLocationClient @Inject constructor(
    private val context: Context
) {
    @SuppressLint("MissingPermission") // Ensure permissions are requested at UI level before calling
    fun getLocationUpdates(intervalMs: Long = 10000L): Flow<Location> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasNetworkProvider = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val hasGpsProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasNetworkProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                intervalMs,
                10f, // min distance in meters
                listener
            )
        } else if (hasGpsProvider) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                intervalMs,
                10f,
                listener
            )
        }

        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }
}
