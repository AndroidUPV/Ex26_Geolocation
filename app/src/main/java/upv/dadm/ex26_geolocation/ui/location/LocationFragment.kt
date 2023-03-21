/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.ui.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import upv.dadm.ex26_geolocation.CHANNEL_ID
import upv.dadm.ex26_geolocation.GeofencingBroadcastReceiver
import upv.dadm.ex26_geolocation.R
import upv.dadm.ex26_geolocation.databinding.FragmentLocationBinding
import upv.dadm.ex26_geolocation.utils.GeocodingException
import upv.dadm.ex26_geolocation.utils.NoGeocoderException
import upv.dadm.ex26_geolocation.utils.NoInternetException
import javax.inject.Inject

/**
 * Displays the current location using its latitude, longitude, and human readable address.
 * It takes care of managing the required permissions to access the location.
 */
// The Hilt annotation @AndroidEntryPoint is required to receive dependencies from its parent class
@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location), MenuProvider {

    // Reference to the ViewModel shared between Fragments
    private val viewModel: LocationViewModel by activityViewModels()

    @Inject
    lateinit var geofencingRequest: GeofencingRequest
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var pendingIntent: PendingIntent

    // Backing property to resource binding
    private var _binding: FragmentLocationBinding? = null

    // Property valid between onCreateView() and onDestroyView()
    private val binding
        get() = _binding!!

    // Request the required permission from the user and requests the current location if granted
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    // Request the current location after granting the access to a fine location
                    viewModel.setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    setupObservers()
                    viewModel.setFineLocationRationaleUnderstood(false)
                    // Create the options menu after enabling geofencing
                    viewModel.setGeofencingEnabled(true)

                }
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    false
                ) -> {
                    // Request the current location after granting the access to a coarse location
                    // THIS WILL NOT WORK IN THE EMULATOR, WHICH ONLY ALLOWS FOR GPS LOCATION
                    // Geofencing is not enabled, as it requires access to fine location
                    viewModel.setPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    setupObservers()
                    viewModel.setFineLocationRationaleUnderstood(false)
                }
                else -> {
                    showSnackbar(R.string.no_permission)
                }
            }
        }

    // Request the required permission from the user and requests the current location if granted
    private val requestBackgroundPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    false
                ) -> {
                    // Request the current location after granting the access to a fine location
                    viewModel.setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    // Geofencing requires background location permission in API < 29
                    if (VERSION.SDK_INT < 29 ||
                        permissions.getOrDefault(
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            false
                        )
                    ) setupGeofencing()
                    addGeofence()
                    viewModel.setBackgroundLocationRationaleUnderstood(false)
                }
                permissions.getOrDefault(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    false
                ) -> {
                    // Request the current location after granting the access to a coarse location
                    // THIS WILL NOT WORK IN THE EMULATOR, WHICH ONLY ALLOWS FOR GPS LOCATION
                    // Geofencing is not enabled, as it requires access to fine location
                    viewModel.setPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    viewModel.setBackgroundLocationRationaleUnderstood(false)
                }
                else -> {
                    showSnackbar(R.string.no_permission)
                }
            }
        }

    // Id of the custom geofence event
    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "LocationFragment.action.ACTION_GEOFENCE_EVENT"
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the automatically generated view binding for the layout resource
        _binding = FragmentLocationBinding.bind(view)

        // Request the location permission from the user once she has understood the rationale
        viewModel.isFineLocationRationaleUnderstood.observe(viewLifecycleOwner) { isUnderstood ->
            if (isUnderstood) {
                // Request permission for just geolocation at first
                requestFineLocationPermission()
            }
        }

        if (VERSION.SDK_INT > 28)
        // Request the background location permission from the user once she has understood the rationale
            viewModel.isBackgroundLocationRationaleUnderstood.observe(viewLifecycleOwner) { isUnderstood ->
                if (isUnderstood) {
                    // Request permission for background permission
                    requestBackgroundPermissions()
                }
            }

        // Check whether Google Play Services are available
        if (isGooglePlayServicesAvailable()) {

            // Check permission just for geolocation
            if (isFineLocationPermissionsGranted()) {
                viewModel.setPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                // Set up geofencing
                setupGeofencing()
                // Set up observers to react to changes in the UI state
                setupObservers()
                // Create the options menu after enabling geofencing
                viewModel.setGeofencingEnabled(true)
            }
            // Check whether a rationale about the needs for this permission must be displayed
            else if (
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                if (viewModel.isFineLocationRationaleUnderstood.value == false)
                // Show a dialog with the required rationale
                    findNavController().currentDestination?.getAction(R.id.actionShowFineLocationRationale)
                        ?.let {
                            findNavController().navigate(R.id.actionShowFineLocationRationale)
                        }
            }
            // Request the required permission from the user
            else requestFineLocationPermission()

        }
        // Otherwise, display a message
        // The app cannot provide any functionality in this case
        else showSnackbar(R.string.no_play_services)

    }

    /**
     * Checks whether the Google Play Services are available on the device.
     */
    private fun isGooglePlayServicesAvailable() =
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext()) == ConnectionResult.SUCCESS

    /**
     * Checks whether geolocation permission is already granted.
     */
    private fun isFineLocationPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Checks whether background location is already granted.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isBackgroundLocationPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Requests permissions to obtain the current location of the device.
     */
    private fun requestFineLocationPermission() =
        requestLocationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

    /**
     * Requests permissions to obtain the current location of the device
     * while the application is both in foreground and background.
     */
    private fun requestBackgroundPermissions() =
        requestBackgroundPermissionsLauncher.launch(
            if (VERSION.SDK_INT < 29) arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            else arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

        )

    private fun setupGeofencing() {
        // Get geofencing client
        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        // Intent that will activate the custom BroadcastReceiver
        val intent = Intent(context, GeofencingBroadcastReceiver::class.java)
            .setAction(ACTION_GEOFENCE_EVENT)
        // Flags must state the mutability of the PendingIntent for API > 30
        pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            if (VERSION.SDK_INT > 30) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
        // Channel where notifications will be posted
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            setShowBadge(false)
            description = getString(R.string.channel_description)
        }
        // Create the desired notification channel
        val notificationManager =
            requireActivity().getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Sets up observers to react to changes in the UI state
     */
    private fun setupObservers() {
        // Display the latitude and longitude of the current location
        viewModel.location.observe(viewLifecycleOwner) { location ->
            if (location == null)
            // Display a message when the current location is null
                showSnackbar(R.string.no_location)
            else {
                binding.tvLatitude.text = location.latitude.toString()
                binding.tvLongitude.text = location.longitude.toString()
                // Translate the current location into a human readable address
                viewModel.getAddress()
            }
        }
        // Display the current location as a human readable address
        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvAddress.text = address.getAddressLine(0)
        }
        // Display an error message when something has gone wrong
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                // Select the message according to the received exception
                val messageId = when (error) {
                    is NoGeocoderException -> R.string.no_geocoding
                    is NoInternetException -> R.string.no_internet
                    is GeocodingException -> R.string.no_translation
                    else -> R.string.unknown_problem
                }
                // Display the message
                showSnackbar(messageId)
                // Clear the address TextView
                binding.tvAddress.text = ""
                // Clear the error flag
                viewModel.clearError()
            }
        }
        // Enable the options menu when geofencing is available
        viewModel.isGeofencingEnabled.observe(viewLifecycleOwner) { isGeofencingEnabled ->
            if (isGeofencingEnabled) requireActivity().addMenuProvider(this)
            else requireActivity().removeMenuProvider(this)
        }
        // Recreate the option menu after the user selects an action element
        viewModel.isGeofencingOnVisible.observe(viewLifecycleOwner) { isVisible ->
            requireActivity().invalidateMenu()
        }
    }

    /**
     * Displays the provided message.
     */
    private fun showSnackbar(messageID: Int) =
        Snackbar.make(binding.root, messageID, Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear resources to make them eligible for garbage collection
        _binding = null
    }

    // Populates the ActionBar with action elements
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
        menuInflater.inflate(R.menu.menu_geofencing, menu)

    // Allows the modification of elements of the already created menu before showing it
    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.mGeofencingOn).isVisible =
            (viewModel.isGeofencingEnabled.value ?: true) and
                    (viewModel.isGeofencingOnVisible.value ?: true)
        menu.findItem(R.id.mGeofencingOff).isVisible =
            (viewModel.isGeofencingEnabled.value ?: true) and
                    (viewModel.isGeofencingOnVisible.value?.not() ?: false)
    }

    /**
     * Add a geofence.
     */
    private fun addGeofence() {
        geofencingClient.addGeofences(geofencingRequest, pendingIntent).run {
            addOnSuccessListener { showSnackbar(R.string.geofence_added) }
            addOnFailureListener { showSnackbar(R.string.geofence_added_failed) }
            viewModel.setGeofenceOnVisible(false)
        }
    }

    // Reacts to the selection of action elements
    override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
        when (menuItem.itemId) {

            // Add a geofence
            R.id.mGeofencingOn -> {
                // Check the required permissions are still granted to add a geofence
                if (isFineLocationPermissionsGranted() && (VERSION.SDK_INT < 29 || isBackgroundLocationPermissionsGranted()))
                    addGeofence()
                // Check whether a rationale about the needs for these permissions must be displayed
                else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                ) {
                    if (viewModel.isBackgroundLocationRationaleUnderstood.value == false)
                    // Show a dialog with the required rationale
                        findNavController().currentDestination?.getAction(R.id.actionShowBackgroundLocationRationale)
                            ?.let {
                                findNavController().navigate(R.id.actionShowBackgroundLocationRationale)
                            }
                } else requestBackgroundPermissions()
                // Update the options menu
                true
            }

            // Remove a geofence
            R.id.mGeofencingOff -> {
                geofencingClient.removeGeofences(pendingIntent).run {
                    addOnSuccessListener { showSnackbar(R.string.geofence_removed) }
                    addOnFailureListener { showSnackbar(R.string.geofence_removed_failed) }
                }
                // Update the options menu
                viewModel.setGeofenceOnVisible(true)
                true
            }
            // If none of the custom action elements was selected, let the system deal with it
            else -> false
        }
}