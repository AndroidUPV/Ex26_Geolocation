/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.data.geocoding

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import upv.dadm.ex26_geolocation.utils.GeocodingException
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * DataSource for translating the current location into a human readable address.
 * Implements the GeocodingDataSource interface.
 */
class GeocodingDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context
) : GeocodingDataSource {

    // Geocoder instance
    private val geocoder = Geocoder(context)

    /**
     * Translates the current location into a human readable address.
     */
    override suspend fun getAddress(location: Location): Result<Address> =
        // Beginning with API 33 Geocoder.getFromLocation() requires a listener to provide the translation
        if (Build.VERSION.SDK_INT > 32) {
            // Operations must be moved to an IO optimised thread
            withContext(Dispatchers.IO) {
                // Suspend the coroutine to wait for the Geocoder to return the translation through the listener
                suspendCancellableCoroutine { continuation ->
                    try {
                        // Expect just 1 address for the given location
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addressList ->
                            // Convert the address to a Result and resume the coroutine
                            continuation.resume(addressListToResult(addressList))
                        }
                    } catch (exception: IOException) {
                        // Generate a failed Result and resume the coroutine
                        continuation.resume(Result.failure(GeocodingException()))
                    }
                    continuation.invokeOnCancellation {
                        // If the coroutine is cancelled, generate a failed Result
                        Result.failure<Address>(GeocodingException())
                    }
                }
            }
        }
        // In previous APIs, Geocoder.getFromLocation() does not use a listener
        // Operations must be moved to an IO optimised thread
        else withContext(Dispatchers.IO) {
            try {
                // Expect just 1 address for the given location
                addressListToResult(
                    geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
                )
            } catch (exception: IOException) {
                // Generate a failed Result
                Result.failure(GeocodingException())
            }
        }

    /**
     * Transforms the list of addresses obtained into a Result<Address>.
     */
    private fun addressListToResult(addressList: List<Address>?): Result<Address> =
        // Return an exception if the list is null or empty
        if (addressList.isNullOrEmpty())
            Result.failure(GeocodingException())
        // Otherwise, return the first address
        else
            Result.success(addressList[0])

}