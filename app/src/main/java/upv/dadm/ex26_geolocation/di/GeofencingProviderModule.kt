/*
 * Copyright (c) 2022
 * David de Andrés and Juan Carlos Ruiz
 * Development of apps for mobile devices
 * Universitat Politècnica de València
 */

package upv.dadm.ex26_geolocation.di

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import upv.dadm.ex26_geolocation.CHANNEL_ID
import upv.dadm.ex26_geolocation.R
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Hilt module that determines how to provide required dependencies
 * for third party components and Builders.
 */
@Module
@InstallIn(SingletonComponent::class)
class GeofencingProviderModule {

    /**
     * Provides an instance of a Geofence, as it is always the same.
     */
    @Provides
    @Singleton
    fun geofenceProvider(): Geofence =
        Geofence.Builder()
            .setRequestId("ETSINF")
            .setCircularRegion(39.48278537396632, -0.34672121870866757, 40.0F)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

    /**
     * Provides an instance of GeofencingRequest.
     */
    @Provides
    @Singleton
    fun geofencingRequestProvider(geofence: Geofence): GeofencingRequest =
        GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class EnterNotification

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ExitNotification

    /**
     * Provides an instance of the notification to display when entering the geofence.
     */
    @Provides
    @Singleton
    @EnterNotification
    fun enterNotification(@ApplicationContext context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_geofencing_on)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_enter))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

    /**
     * Provides an instance of the notification to display when exiting the geofence.
     */
    @Provides
    @Singleton
    @ExitNotification
    fun exitNotification(@ApplicationContext context: Context): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_geofencing_on)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_exit))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
}