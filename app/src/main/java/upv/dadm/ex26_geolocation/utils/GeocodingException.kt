/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.utils

import java.io.IOException

/**
 * Exception used to notify that something unexpected happened
 * when trying to translate the current location into a human readable address.
 */
class GeocodingException : IOException()