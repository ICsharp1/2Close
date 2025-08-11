# Geofencing App

This is a simple Android application that demonstrates the use of Google Maps, Fused Location Provider, and the Geofencing API.

## About the App

The app allows a user to set a target location on a map, either by typing an address, providing coordinates, or tapping on the map. It then uses the Geofencing API to create a geofence around that location. When the user's device enters the geofenced area (within 200 meters of the target), the app triggers a high-priority notification and plays an alarm sound.

## Features

- **Google Maps Integration:** Displays a Google Map as the main UI.
- **Set Target Location:**
    - Enter an address or coordinates in a text field.
    - Tap on the map to select a location.
- **Current Location:** Shows the user's current location on the map.
- **Geofencing:** Creates a geofence around the target location.
- **Notifications:** Triggers a high-priority notification upon entering the geofence.
- **Alarm Sound:** Plays a short alarm sound with the notification.
- **Runtime Permissions:** Handles all necessary location permissions (foreground and background).
- **Material Design:** Uses Material Design components for the UI.

## Settings

You can access the settings page by tapping the settings icon in the toolbar on the main screen. The settings page allows you to customize the following:

-   **Geofence Radius:** Adjust the slider to set the distance for the geofence trigger (from 50 to 1000 meters).
-   **Dark Mode:** Use the switch to toggle between light and dark themes for the application.

## Setup Instructions

To build and run this app, you will need to provide your own Google Maps API key.

### 1. Get a Google Maps API Key

Follow the instructions on the Google Cloud Platform documentation to get an API key:
[https://developers.google.com/maps/documentation/android-sdk/get-api-key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

Make sure to enable the **Maps SDK for Android** and the **Geocoder API** for your project in the Google Cloud Console.

### 2. Add the API Key to the Project

Once you have your API key, you need to add it to the `AndroidManifest.xml` file.

1.  Open the file `app/src/main/AndroidManifest.xml`.
2.  Find the following line:
    ```xml
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY" />
    ```
3.  Replace `"YOUR_API_KEY"` with your actual Google Maps API key.

## Building and Running

1.  Open the project in Android Studio.
2.  Let Gradle sync and download the required dependencies.
3.  Connect an Android device or start an emulator.
4.  Run the app from Android Studio.

## Alarm Sound

The app uses the default alarm sound. You can replace the placeholder file `app/src/main/res/raw/alarm_sound.mp3` with your own sound file. If you do, make sure to update the code in `GeofenceBroadcastReceiver.kt` to use your new sound file if you change the name.
