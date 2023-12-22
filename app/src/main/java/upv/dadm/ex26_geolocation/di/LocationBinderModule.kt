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

package upv.dadm.ex26_geolocation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import upv.dadm.ex26_geolocation.data.location.LocationDataSource
import upv.dadm.ex26_geolocation.data.location.LocationDataSourceImpl
import upv.dadm.ex26_geolocation.data.location.LocationRepository
import upv.dadm.ex26_geolocation.data.location.LocationRepositoryImpl
import javax.inject.Singleton

/**
 * Hilt module that determines how to provide required dependencies for interfaces.
 */
@Module
// The Hilt annotation @SingletonComponent creates and destroy instances following the lifecycle of the Application
@InstallIn(SingletonComponent::class)
abstract class LocationBinderModule {

    /**
     * Provides an instance of a LocationDataSource.
     */
    @Binds
    @Singleton
    abstract fun bindLocationDataSource(
        locationDataSourceImpl: LocationDataSourceImpl
    ): LocationDataSource

    /**
     * Provides an instance of a LocationRepository.
     */
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
}