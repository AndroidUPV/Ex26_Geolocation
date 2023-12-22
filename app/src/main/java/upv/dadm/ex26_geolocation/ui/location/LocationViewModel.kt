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

package upv.dadm.ex26_geolocation.ui.location

import android.location.Address
import android.location.Location
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

    // Backing property for the permission currently granted by the user
    private val _permission = MutableLiveData<String>()

    // Current location
    val location: LiveData<Location?> = _permission.switchMap { permission ->
        locationRepository.getLocationUpdates(permission).asLiveData()
    }

    // Backing property for geofencing being enabled
    private val _isGeofencingEnabled = MutableLiveData(false)

    // Geofencing is enabled
    val isGeofencingEnabled: LiveData<Boolean> = _isGeofencingEnabled

    // Backing property for the visibility of the options menu to add a geofence
    private val _isGeofencingOnVisible = MutableLiveData(true)

    // Visibility of the options menu to add a geofence
    val isGeofencingOnVisible: LiveData<Boolean> = _isGeofencingOnVisible

    // Backing property for whether the user has understood the necessity for location permission
    private val _isFineLocationRationaleUnderstood = MutableLiveData(false)

    // Whether the user has understood the rationale for the necessity for location permission
    val isFineLocationRationaleUnderstood: LiveData<Boolean> = _isFineLocationRationaleUnderstood

    // Backing property for whether the user has understood the necessity for background location permission
    private val _isBackgroundLocationRationaleUnderstood = MutableLiveData(false)

    // Whether the user has understood the rationale for the necessity for background location permission
    val isBackgroundLocationRationaleUnderstood: LiveData<Boolean> = _isBackgroundLocationRationaleUnderstood

    // Backing property for the human readable address of the current location
    private val _address = MutableLiveData<Address>()

    // Human readable address of the current location
    val address: LiveData<Address> = _address

    // Backing property for the error received
    private val _error = MutableLiveData<Throwable?>()

    // Error received
    val error: LiveData<Throwable?> = _error

    /**
     * Sets the permissions currently granted by the user.
     */
    fun setPermission(permission: String) {
        _permission.value = permission
    }

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
     * Sets whether the user has understood the rationale for the necessity of location permissions.
     */
    fun setFineLocationRationaleUnderstood(understood: Boolean) {
        _isFineLocationRationaleUnderstood.value = understood
    }

    /**
     * Sets whether the user has understood the rationale for the necessity of background location permissions.
     */
    fun setBackgroundLocationRationaleUnderstood(understood: Boolean) {
        _isBackgroundLocationRationaleUnderstood.value = understood
    }

    /**
     * Clears the error received.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Sets whether geofencing is enabled.
     */
    fun setGeofencingEnabled(isEnabled: Boolean) {
        _isGeofencingEnabled.value = isEnabled
    }

    /**
     * Sets whether the options menu for adding a geofence should be visible.
     */
    fun setGeofenceOnVisible(isVisible: Boolean) {
        _isGeofencingOnVisible.value = isVisible
    }
}