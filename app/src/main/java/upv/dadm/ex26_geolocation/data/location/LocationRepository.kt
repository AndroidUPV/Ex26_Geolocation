/*
 * Copyright (c) 2022-2023 Universitat Politècnica de València
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

/**
 * Interface declaring the methods that the Repository exposes to ViewModels.
 */
interface LocationRepository {

    /**
     * Returns a Flow with updates of the current location.
     */
    fun getLocationUpdates(permission: String): Flow<Location?>

}