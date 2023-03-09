/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.ui.location

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import upv.dadm.ex26_geolocation.R

/**
 * Displays the user a dialog with a description of why permissions must be granted
 * for a given for the desired functionality.
 */
class LocationRationaleDialogFragment(
    private val locationRationaleListener: LocationRationaleListener
) : DialogFragment() {

    /**
     * Interface for the user to confirm that she has understood the necessity of required permissions.
     */
    interface LocationRationaleListener {
        fun onUnderstood()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        // Create the dialog with a single Button to dismiss it
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.rationale_title)
            .setMessage(R.string.rationale_message)
            .setPositiveButton(R.string.understood) { _, _ ->
                locationRationaleListener.onUnderstood()
            }
            .create()
}