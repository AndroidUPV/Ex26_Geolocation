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

package upv.dadm.ex26_geolocation.ui.location

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val _permission = MutableStateFlow("")

    // Current location
    @OptIn(ExperimentalCoroutinesApi::class)
    val location = _permission.flatMapLatest { permission ->
        locationRepository.getLocationUpdates(permission)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    // Backing property for geofencing being enabled
    private val _isGeofencingEnabled = MutableStateFlow(false)

    // Geofencing is enabled
    val isGeofencingEnabled = _isGeofencingEnabled.asStateFlow()

    // Backing property for the visibility of the options menu to add a geofence
    private val _isGeofencingOnVisible = MutableStateFlow(true)

    // Visibility of the options menu to add a geofence
    val isGeofencingOnVisible = _isGeofencingOnVisible.asStateFlow()

    // Backing property for whether the user has understood the necessity for location permission
    private val _isFineLocationRationaleUnderstood = MutableStateFlow(false)

    // Whether the user has understood the rationale for the necessity for location permission
    val isFineLocationRationaleUnderstood = _isFineLocationRationaleUnderstood.asStateFlow()

    // Backing property for whether the user has understood the necessity for background location permission
    private val _isBackgroundLocationRationaleUnderstood = MutableStateFlow(false)

    // Whether the user has understood the rationale for the necessity for background location permission
    val isBackgroundLocationRationaleUnderstood =
        _isBackgroundLocationRationaleUnderstood.asStateFlow()

    // Backing property for the human readable address of the current location
    private val _address = MutableStateFlow<Address?>(null)

    // Human readable address of the current location
    val address = _address.asStateFlow()

    // Backing property for the error received
    private val _error = MutableStateFlow<Throwable?>(null)

    // Error received
    val error = _error.asStateFlow()

    /**
     * Sets the permissions currently granted by the user.
     */
    fun setPermission(permission: String) =
        _permission.update { permission }

    /**
     * Translates the current location into a human readable address.
     */
    fun getAddress() {
        // Caches the current location, in case it changes along this operation
        location.value?.let { cachedLocation ->
            // As it is a blocking operation it should be executed in a thread
            viewModelScope.launch {
                // Get a human readable address form the current location
                geocodingRepository.getAddress(cachedLocation)
                    // Check the result
                    .fold(
                        onSuccess = { result -> _address.update { result } },
                        onFailure = { throwable -> _error.update { throwable } }
                    )
            }
        }
    }

    /**
     * Sets whether the user has understood the rationale for the necessity of location permissions.
     */
    fun setFineLocationRationaleUnderstood(understood: Boolean) =
        _isFineLocationRationaleUnderstood.update { understood }

    /**
     * Sets whether the user has understood the rationale for the necessity of background location permissions.
     */
    fun setBackgroundLocationRationaleUnderstood(understood: Boolean) =
        _isBackgroundLocationRationaleUnderstood.update { understood }

    /**
     * Clears the error received.
     */
    fun clearError() =
        _error.update { null }

    /**
     * Sets whether geofencing is enabled.
     */
    fun setGeofencingEnabled(isEnabled: Boolean) =
        _isGeofencingEnabled.update { isEnabled }

    /**
     * Sets whether the options menu for adding a geofence should be visible.
     */
    fun setGeofenceOnVisible(isVisible: Boolean) =
        _isGeofencingOnVisible.update { isVisible }
}