# Ex26_Geolocation

Lecture 07 - Geolocation and Maps

The app displays the current location of device, including its latitude, longitude, and human readable address.
Google Play Services are used to obtain continuous updates of the device location.

A Geocoder is used to translate the current location into a human readable address.

A Geofence is setup 50 metres around the ETSINF.
A BroadcastReceiver will receive an Intent whenever the device enters or exists this Geofence and it will display a notification accordingly.
The options menu allows the user to add or remove this Geofence.

All permissions are checked and requested at runtime.
Code is extremely ugly as different methods are used depending on the API version of the device.

IMPORTANT:
The emulator does not provide a Network Provider, so locations can only be obtained through the GPS Provider (ACCESS_FINE_LOCATION must be granted).
Likewise, when the application is in the foreground it will display notifications when entering/exiting the Geofence, but it will not displays the notifications when it is in the background.
Everything works fine in a real phone. 