/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
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