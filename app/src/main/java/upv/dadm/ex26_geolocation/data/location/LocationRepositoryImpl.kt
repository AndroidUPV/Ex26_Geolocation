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
    override fun getLocationUpdates(permission: String): Flow<Location?> =
        locationDataSource.getLocationUpdates(permission)

}