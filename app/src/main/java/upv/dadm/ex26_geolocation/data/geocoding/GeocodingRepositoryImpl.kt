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
import android.location.Geocoder
import android.location.Location
import upv.dadm.ex26_geolocation.utils.ConnectivityChecker
import upv.dadm.ex26_geolocation.utils.NoGeocoderException
import upv.dadm.ex26_geolocation.utils.NoInternetException
import javax.inject.Inject

/**
 * Repository for translating the current location into a human readable address.
 * Implements the GeocodingRepository interface.
 */
class GeocodingRepositoryImpl @Inject constructor(
    private val connectivityChecker: ConnectivityChecker,
    private val geocodingDataSource: GeocodingDataSource
) : GeocodingRepository {

    /**
     * Translates the current location into a human readable address.
     */
    override suspend fun getAddress(location: Location): Result<Address> =
        // Obtain the address if there is any Geocoder available
        if (Geocoder.isPresent())
        // Obtain the address if there is Internet connectivity available
            if (connectivityChecker.isConnectionAvailable())
                geocodingDataSource.getAddress(location)
            // Otherwise, an error message
            else Result.failure(NoInternetException())
        // Otherwise, an error message
        else Result.failure(NoGeocoderException())

}