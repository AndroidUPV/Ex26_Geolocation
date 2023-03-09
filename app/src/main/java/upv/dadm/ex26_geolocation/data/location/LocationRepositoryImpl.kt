/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.data.location

import android.location.Location
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for obtaining continuous updates of the current location.
 * Implements the LocationRepository interface.
 */
class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource
) : LocationRepository {

    /**
     * Returns a Flow with updates of the current location.
     */
    override fun getLocationUpdates(): Flow<Location?> =
        locationDataSource.getLocationUpdates()

}