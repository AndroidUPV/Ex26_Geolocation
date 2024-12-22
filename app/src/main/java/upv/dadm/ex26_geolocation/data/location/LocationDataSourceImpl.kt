/*
 * Copyright (c) 2022-2024 Universitat Politècnica de València
 * Authors: David de Andrés and Juan Carlos Ruiz
 *          Fault-Tolerant Systems
 *          Instituto ITACA
 *          Universitat Politècnica de València
 *
 * Distributed under MIT license
 * (See accompanying file LICENSE.txt)
 */

package upv.dadm.ex26_geolocation.data.location

import android.Manifest
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import upv.dadm.ex26_geolocation.di.LocationProviderModule
import javax.inject.Inject

/**
 * DataSource for obtaining continuous updates of the current location.
 * Implements the LocationDataSource interface.
 */
class LocationDataSourceImpl @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    @LocationProviderModule.HighAccuracyRequest private val highAccuracyRequest: LocationRequest,
    @LocationProviderModule.BalancedPowerAccuracyRequest private val balancedPowerAccuracyRequest: LocationRequest,
) : LocationDataSource {

    /**
     * Returns a Flow with updates of the current location.
     */
    override fun getLocationUpdates(permission: String): Flow<Location?> = callbackFlow {
        // Define the listener that will be executed whenever a new location is available
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                // Send the updated location
                trySend(location.lastLocation)
            }
        }
        if (permission.isNotEmpty())
        // Register the listener to receive updates on current location
            fusedLocationProviderClient.requestLocationUpdates(
                when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> highAccuracyRequest
                    Manifest.permission.ACCESS_COARSE_LOCATION -> balancedPowerAccuracyRequest
                    // This should never happen
                    else -> balancedPowerAccuracyRequest
                },
                locationCallback,
                Looper.getMainLooper()
            )
        else
        // Provide an empty location
            Location("").apply {
                latitude = 0.0
                longitude = 0.0
            }
        // Unregister the listener when the channel is closed or cancelled
        awaitClose { fusedLocationProviderClient.removeLocationUpdates(locationCallback) }
    }
        // Operations must be moved to an IO optimised thread
        .flowOn(Dispatchers.IO)
}