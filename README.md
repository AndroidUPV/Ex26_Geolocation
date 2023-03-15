# Ex26_Geolocation

Lecture 07 - Geolocation and Maps

The app displays the current location of device, including its latitude, longitude, and human readable address.
Google Play Services are used to obtain continuous updates of the device location.
A Geocoder is used to translate the current location into a human readable address.

All permissions are checked and requested at runtime.

IMPORTANT:
The emulator does not provide a Network Provider, so locations can only be obtained through the GPS Provider (ACCESS_FINE_LOCATION must be granted).
It works fine in a real phone. 