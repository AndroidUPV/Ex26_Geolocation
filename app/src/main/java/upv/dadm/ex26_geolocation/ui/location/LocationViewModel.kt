/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.ui.location

import android.location.Address
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import upv.dadm.ex26_geolocation.data.geocoding.GeocodingRepository
import upv.dadm.ex26_geolocation.data.location.LocationRepository
import javax.inject.Inject

/**
 * Holds information about the current location of the device.
 */
// The Hilt annotation @HiltEntryPoint is required to receive dependencies from its parent class
@HiltViewModel
class LocationViewModel @Inject constructor(
    locationRepository: LocationRepository,
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    // Current location
    val location = locationRepository.getLocationUpdates().asLiveData()

    // Backing property for whether the user has understood the rationale for the required permissions
    private val _isRationaleUnderstood = MutableLiveData(false)

    // Whether the user has understood the rationale for the required permissions
    val isRationaleUnderstood: LiveData<Boolean>
        get() = _isRationaleUnderstood

    // Backing property for the human readable address of the current location
    private val _address = MutableLiveData<Address>()

    // Human readable address of the current location
    val address: LiveData<Address> = _address

    // Backing property for the error received
    private val _error = MutableLiveData<Throwable?>()

    // Error received
    val error: LiveData<Throwable?> = _error

    /**
     * Translates the current location into a human readable address.
     */
    fun getAddress() {
        // Caches the current location, in case it changes along this operation
        val cachedLocation = location.value
        if (cachedLocation != null) {
            // As it is a blocking operation it should be executed in a thread
            viewModelScope.launch {
                // Get a human readable address form the current location
                geocodingRepository.getAddress(cachedLocation)
                    // Check the result
                    .fold(
                        onSuccess = { result -> _address.value = result },
                        onFailure = { throwable -> _error.value = throwable }
                    )
            }
        }
    }

    /**
     * Sets whether the user has understood the rationale for the required permissions.
     */
    fun setRationaleUnderstood(understood: Boolean) {
        _isRationaleUnderstood.value = understood
    }

    /**
     * Clears the error received.
     */
    fun clearError() {
        _error.value = null
    }

}