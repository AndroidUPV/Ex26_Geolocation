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

package upv.dadm.ex26_geolocation

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import upv.dadm.ex26_geolocation.di.GeofencingProviderModule
import upv.dadm.ex26_geolocation.ui.location.LocationFragment
import javax.inject.Inject

// Id of the geofencing channel
const val CHANNEL_ID = "GEOFENCING_CHANNEL"

/**
 * Receives and handles custom geofencing broadcast Intents.
 * It displays a notification when entering and exiting the geofence set at ETSINF.
 * If notifications are not enabled then it shows a Toast.
 */
@AndroidEntryPoint
class GeofencingBroadcastReceiver : BroadcastReceiver() {

    // Custom notification Id
    private companion object {
        const val notificationId = 10
    }

    @Inject
    @GeofencingProviderModule.EnterNotification
    // Notification to display when entering the geofence
    lateinit var enterNotification: Notification

    @Inject
    @GeofencingProviderModule.ExitNotification
    // Notification to display when exiting the geofence
    lateinit var exitNotification: Notification

    /**
     * This method is executed when it receives an Intent broadcast.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // Check whether the received intent is of  the custom geofencing type
        if (intent?.action == LocationFragment.ACTION_GEOFENCE_EVENT) {
            if (context != null) {
                // Get the geofencing event
                val geofencingEvent = GeofencingEvent.fromIntent(intent)
                // Log the error it it has one
                if (geofencingEvent?.hasError() != false) {
                    Log.d(
                        "GEOFENCING",
                        GeofenceStatusCodes.getStatusCodeString(
                            geofencingEvent?.errorCode ?: GeofenceStatusCodes.ERROR
                        )
                    )
                } else {
                    // The device has entered the geofence
                    if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        // Check the required permission to post notifications is granted
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Display a notification if they are enabled
                            if (NotificationManagerCompat.from(context).areNotificationsEnabled())
                                NotificationManagerCompat.from(context)
                                    .notify(notificationId, enterNotification)
                            // Display a Toast otherwise
                            else
                                Toast.makeText(
                                    context,
                                    R.string.notification_enter,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                        // Display a Toast otherwise
                        else
                            Toast.makeText(context, R.string.notification_enter, Toast.LENGTH_SHORT)
                                .show()

                    }
                    // The device has exited the geofence
                    else if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                        // Check the required permission to post notifications is granted
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            // Display a notification if they are enabled
                            if (NotificationManagerCompat.from(context).areNotificationsEnabled())
                                NotificationManagerCompat.from(context)
                                    .notify(notificationId, exitNotification)
                            // Display a Toast otherwise
                            else
                                Toast.makeText(
                                    context,
                                    R.string.notification_exit,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                        }
                        // Display a Toast otherwise
                        else
                            Toast.makeText(context, R.string.notification_exit, Toast.LENGTH_SHORT)
                                .show()

                    }
                }
            }
        }
    }

}