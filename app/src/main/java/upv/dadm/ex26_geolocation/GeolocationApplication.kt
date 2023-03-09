/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application required for Hilt to inject dependencies.
 * The Manifest must declare it: <application android:name = ".GeolocationApplication"/>
 */
// The Hilt annotation @HiltAndroidApp identifies this class as the dependency container
@HiltAndroidApp
class GeolocationApplication : Application()