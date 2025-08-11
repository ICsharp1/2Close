package com.example.geofencingapp

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // UI Elements
    private lateinit var mapView: MapView
    private lateinit var locationEditText: EditText
    private lateinit var setTargetButton: Button

    // Map and Location
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private var geofencePendingIntent: PendingIntent? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val GEOFENCE_ID = "TARGET_GEOFENCE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        mapView = findViewById(R.id.mapView)
        locationEditText = findViewById(R.id.locationEditText)
        setTargetButton = findViewById(R.id.setTargetButton)

        // Initialize MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Initialize location and geofencing clients
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        // Set button click listener
        setTargetButton.setOnClickListener {
            val locationName = locationEditText.text.toString()
            if (locationName.isNotEmpty()) {
                setTargetFromAddress(locationName)
            } else {
                Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermissions()

        // Set listener for map clicks
        googleMap.setOnMapClickListener { latLng ->
            setTargetLocation(latLng)
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request fine location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Fine location permission is granted, now check for background location
            checkBackgroundLocationPermission()
        }
    }

    private fun checkBackgroundLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request background location permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), LOCATION_PERMISSION_REQUEST_CODE + 1)
        } else {
            // All permissions are granted
            setupMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBackgroundLocationPermission() // Check for background permission next
            } else {
                Toast.makeText(this, "Fine location permission is required", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE + 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap()
            } else {
                Toast.makeText(this, "Background location permission is required for geofencing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }
    }

    private fun setTargetFromAddress(address: String) {
        val geocoder = Geocoder(this)
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)
                setTargetLocation(latLng)
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Geocoding failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setTargetLocation(latLng: LatLng) {
        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(latLng).title("Target Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        addGeofence(latLng)
    }

    private fun addGeofence(latLng: LatLng) {
        val sharedPreferences = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val radius = sharedPreferences.getInt(SettingsActivity.KEY_RADIUS, 200).toFloat()

        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(latLng.latitude, latLng.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent()).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Geofence added", Toast.LENGTH_SHORT).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "Failed to add geofence: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent as PendingIntent
        }
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        return geofencePendingIntent as PendingIntent
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // MapView lifecycle methods
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}
