/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.data.location

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * DataSource for obtaining continuous updates of the current location.
 * Implements the LocationDataSource interface.
 */
class LocationDataSourceImpl @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val locationRequest: LocationRequest
) : LocationDataSource {

    /**
     * Returns a Flow with updates of the current location.
     */
    override fun getLocationUpdates(): Flow<Location?> = callbackFlow {
        // Define the listener that will be executed whenever a new location is available
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(location: LocationResult) {
                // Send the updated location
                trySend(location.lastLocation)
            }
        }
        // Register the listener to receive updates on current location
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            Executors.newSingleThreadExecutor(),
            locationCallback
        )
        // Unregister the listener when the channel is closed or cancelled
        awaitClose { fusedLocationProviderClient.removeLocationUpdates(locationCallback) }
    }
        // Operations must be moved to an IO optimised thread
        .flowOn(Dispatchers.IO)
}