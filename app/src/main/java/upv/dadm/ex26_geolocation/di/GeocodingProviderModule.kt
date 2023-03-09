/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import upv.dadm.ex26_geolocation.utils.ConnectivityChecker

/**
 * Hilt module that determines how to provide required dependencies
 * for third party components and Builders.
 */
@Module
// The Hilt annotation @SingletonComponent creates and destroy instances following the lifecycle of the Application
@InstallIn(SingletonComponent::class)
class GeocodingProviderModule {

    /**
     * Provides an instance of ConnectivityChecker.
     */
    @Provides
    fun provideConnectivityChecker(@ApplicationContext context: Context): ConnectivityChecker =
        ConnectivityChecker.getInstance(context)
}