package com.example.gaurdiancircle

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gaurdiancircle.responses.GuardedLocationResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.google.android.gms.location.*
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Guardiansmapview : AppCompatActivity() {

    private lateinit var mapView: MapView
    private var userMarker: Marker? = null
    private var sharedUserMarker: Marker? = null

    private var userPolyline: Polyline? = null
    private val userRoutePoints = mutableListOf<GeoPoint>()

    private var sharedPolyline: Polyline? = null
    private val sharedRoutePoints = mutableListOf<GeoPoint>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 5000 // 5 seconds

    private var firstUserUpdate = true
    private var firstSharedUpdate = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guardiansmapview)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        startLocationUpdates()
        startSharedLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(3000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)

                if (userMarker == null) {
                    userMarker = Marker(mapView).apply {
                        title = "You"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(userMarker)
                }
                userMarker?.position = geoPoint

                if (firstUserUpdate) {
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(geoPoint)
                    firstUserUpdate = false
                }

                // Track route
                userRoutePoints.add(geoPoint)
                if (userPolyline == null) {
                    userPolyline = Polyline().apply {
                        width = 8f
                        color = 0xFF0000FF.toInt() // Blue
                    }
                    mapView.overlays.add(userPolyline)
                }
                userPolyline?.setPoints(userRoutePoints)

                mapView.invalidate()
            }
        }
    }

    // Periodic guardian location updates
    private fun startSharedLocationUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                fetchAndDisplaySharedLocation()
                handler.postDelayed(this, updateInterval)
            }
        }, updateInterval)
    }

    private fun fetchAndDisplaySharedLocation() {
        val guardianEmail = "vijay1@gmail.com"

        RetrofitClient.instance.getSharedLocation(guardianEmail).enqueue(object : Callback<GuardedLocationResponse> {
            override fun onResponse(call: Call<GuardedLocationResponse>, response: Response<GuardedLocationResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "success" && body.location != null) {
                        val lat = body.location.latitude.toDoubleOrNull()
                        val lon = body.location.longitude.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            val geoPoint = GeoPoint(lat, lon)

                            if (sharedUserMarker == null) {
                                sharedUserMarker = Marker(mapView).apply {
                                    title = "Guardian"
                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                }
                                mapView.overlays.add(sharedUserMarker)
                            }
                            sharedUserMarker?.position = geoPoint

                            if (firstSharedUpdate) {
                                mapView.controller.setZoom(15.0)
                                mapView.controller.setCenter(geoPoint)
                                firstSharedUpdate = false
                            }

                            // Track guardian route
                            sharedRoutePoints.add(geoPoint)
                            if (sharedPolyline == null) {
                                sharedPolyline = Polyline().apply {
                                    width = 8f
                                    color = 0xFFFF0000.toInt() // Red
                                }
                                mapView.overlays.add(sharedPolyline)
                            }
                            sharedPolyline?.setPoints(sharedRoutePoints)

                            mapView.invalidate()
                        }
                    }
                } else {
                    Toast.makeText(this@Guardiansmapview, "Failed to fetch shared location", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GuardedLocationResponse>, t: Throwable) {
                Toast.makeText(this@Guardiansmapview, "Error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacksAndMessages(null)
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
