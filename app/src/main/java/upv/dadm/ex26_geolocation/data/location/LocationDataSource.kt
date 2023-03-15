/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.data.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

/**
 * Interface declaring the methods that the DataSource exposes to Repositories.
 */
interface LocationDataSource {

    /**
     * Returns a Flow with updates of the current location.
     */
    fun getLocationUpdates(permission: String): Flow<Location?>
}