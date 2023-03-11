/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.ui.location

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import upv.dadm.ex26_geolocation.R
import upv.dadm.ex26_geolocation.databinding.FragmentLocationBinding
import upv.dadm.ex26_geolocation.utils.GeocodingException
import upv.dadm.ex26_geolocation.utils.NoGeocoderException
import upv.dadm.ex26_geolocation.utils.NoInternetException

/**
 * Displays the current location using its latitude, longitude, and human readable address.
 * It takes care of managing the required permissions to access the location.
 */
// The Hilt annotation @AndroidEntryPoint is required to receive dependencies from its parent class
@AndroidEntryPoint
class LocationFragment : Fragment(R.layout.fragment_location) {

    // Reference to the ViewModel shared between Fragments
    private val viewModel: LocationViewModel by activityViewModels()

    // Backing property to resource binding
    private var _binding: FragmentLocationBinding? = null

    // Property valid between onCreateView() and onDestroyView()
    private val binding
        get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                setupObservers()
                viewModel.setRationaleUnderstood(false)
            } else showSnackbar(R.string.no_permission)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the automatically generated view binding for the layout resource
        _binding = FragmentLocationBinding.bind(view)

        // Check whether Google Play Services are available
        if (isGooglePlayServicesAvailable()) {

            // Request the required permission from the user once she has understood the rationale
            viewModel.isRationaleUnderstood.observe(viewLifecycleOwner) { isUnderstood ->
                if (isUnderstood) {
                    requestPermission()
                }
            }

            // Check whether the required permission is granted
            if (isPermissionGranted())
            // Set up observers to react to changes in the UI state
                setupObservers()
            // Check whether a rationale about the needs for this permission must be displayed
            else if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (viewModel.isRationaleUnderstood.value == false)
                // Show a dialog with the required rationale
                    findNavController().currentDestination?.getAction(R.id.actionLocationRationaleDialogFragment)
                        ?.let {
                            findNavController().navigate(R.id.actionLocationRationaleDialogFragment)
                        }
            }
            // Request the required permission from the user
            else requestPermission()

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
     * Checks whether the required permission is already granted.
     */
    private fun isPermissionGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Requests permissions to obtain the current location of the device.
     */
    private fun requestPermission() =
        requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

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
}