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

package upv.dadm.ex26_geolocation.data.geocoding

import android.location.Address
import android.location.Location

/**
 * Interface declaring the methods that the DataSource exposes to Repositories.
 */
interface GeocodingDataSource {

    /**
     * Translates the current location into a human readable address.
     */
    suspend fun getAddress(location: Location): Result<Address>

}