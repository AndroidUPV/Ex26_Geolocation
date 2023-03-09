/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI

/**
 * Singleton object that checks whether there is Internet connectivity.
 */
class ConnectivityChecker private constructor(private val connectivityManager: ConnectivityManager) {

    // @Volatile and synchronized() ensure that this Singleton creation is thread-safe
    companion object {
        @Volatile
        private lateinit var instance: ConnectivityChecker

        fun getInstance(context: Context): ConnectivityChecker {
            synchronized(this) {
                if (!::instance.isInitialized) {
                    // Get a reference to the ConnectivityManager
                    instance =
                        ConnectivityChecker(context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager)
                }
                return instance
            }
        }
    }

    /**
     * Returns whether Internet connectivity is available or not
     * (requires permission to ACCESS_NETWORK_STATE).
     */
    fun isConnectionAvailable(): Boolean {
        // Get the capabilities of the currently active network (whichever it is)
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        // Check whether there is Internet connectivity through the cellular or the WiFi networks
        return (capabilities != null &&
                (capabilities.hasTransport(TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(TRANSPORT_WIFI))
                )
    }
}