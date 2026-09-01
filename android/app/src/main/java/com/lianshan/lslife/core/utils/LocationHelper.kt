package com.qingyuan.lslife.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {
    
    @SuppressLint("MissingPermission")
    suspend fun getCurrentTown(context: Context): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            var location: Location? = null
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider)
                if (l != null) {
                    if (location == null || l.time > location.time) {
                        location = l
                    }
                }
            }

            if (location == null) {
                location = suspendCancellableCoroutine { cont ->
                    val listener = object : android.location.LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            locationManager.removeUpdates(this)
                            if (cont.isActive) cont.resume(loc)
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                    try {
                        locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 
                            0L, 
                            0f, 
                            listener, 
                            android.os.Looper.getMainLooper()
                        )
                        // Timeout safety
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            locationManager.removeUpdates(listener)
                            if (cont.isActive) cont.resume(null)
                        }, 5000)
                    } catch (e: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
            
            if (location == null) return@withContext null

            val geocoder = Geocoder(context, Locale.SIMPLIFIED_CHINESE)
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0) ?: ""
                
                val townRegex = Regex("(?<=县|区|市)[^县区市]+?(镇|街道|乡)")
                val match = townRegex.find(fullAddress)
                
                if (match != null) {
                    return@withContext match.value
                }
                
                val subLocality = address.subLocality ?: ""
                val locality = address.locality ?: ""
                
                if (subLocality.isNotEmpty()) return@withContext subLocality
                if (locality.isNotEmpty()) return@withContext locality
                
                return@withContext fullAddress.take(5)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
