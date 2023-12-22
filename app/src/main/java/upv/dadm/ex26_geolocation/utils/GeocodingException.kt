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

package upv.dadm.ex26_geolocation.utils

import java.io.IOException

/**
 * Exception used to notify that something unexpected happened
 * when trying to translate the current location into a human readable address.
 */
class GeocodingException : IOException()