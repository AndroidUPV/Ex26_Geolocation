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

package upv.dadm.ex26_geolocation.di

import android.content.Context
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module that determines how to provide required dependencies
 * for third party components and Builders.
 */
@Module
// The Hilt annotation @SingletonComponent creates and destroy instances following the lifecycle of the Application
@InstallIn(SingletonComponent::class)
class LocationProviderModule {

    /**
     * Provides an instance of FusedLocationProviderClient.
     */
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class HighAccuracyRequest

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BalancedPowerAccuracyRequest

    /**
     * Provides an instance of LocationRequest.
     */
    @Provides
    @Singleton
    @HighAccuracyRequest
    fun provideHighAccuracyLocationRequest() =
    // Obtain location updates every second, with high accuracy,
        // if the location has changed in, at least, 50 metres
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateDistanceMeters(50.0F)
            .build()

    @Provides
    @Singleton
    @BalancedPowerAccuracyRequest
    fun provideBalancedPowerAccuracyLocationRequest() =
    // Obtain location updates every second, with balanced power and accuracy,
        // if the location has changed in, at least, 50 metres
        LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000)
            .setMinUpdateDistanceMeters(50.0F)
            .build()

}