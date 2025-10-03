package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gaurdiancircle.utils.Utils
import com.google.android.gms.location.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class guardiandashboardfragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private val handler = Handler(Looper.getMainLooper())
    private var isSosScreenOpen = false

    // Replace with your devtunnel URL
    private val BASE_URL = "https://jczx8mjg-80.inc1.devtunnels.ms/gaurdian_circle"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_guardiandashboardfragment, container, false)

        val savedName = Utils.getUserName(requireContext())
        view.findViewById<TextView>(R.id.welcomeText).text = "Hi ${savedName ?: "User"},"
        view.findViewById<TextView>(R.id.tvProfileName).text = savedName ?: "User"

        // Map
        mapView = view.findViewById(R.id.mapHome)
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", 0)
        )
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.setZoom(16.0)
                    mapView.controller.setCenter(geoPoint)

                    val marker = Marker(mapView)
                    marker.position = geoPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Guardian: ${Utils.getUserName(requireContext()) ?: "You"}"
                    mapView.overlays.clear()
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                }
            }
        }

        startLocationUpdates()

        // Buttons
        view.findViewById<LinearLayout>(R.id.viewLiveLocation).setOnClickListener {
            startActivity(Intent(requireContext(), Guardiansmapview::class.java))
        }
        view.findViewById<LinearLayout>(R.id.settingsLayout).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSettings::class.java))
        }
        view.findViewById<View>(R.id.profileCard).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSettings::class.java))
        }
        view.findViewById<View>(R.id.userss).setOnClickListener {
            startActivity(Intent(requireContext(), Acceptguardian::class.java))
        }
        view.findViewById<View>(R.id.chat1).setOnClickListener {
            startActivity(Intent(requireContext(), activity_guardian_chat::class.java))
        }

        startSosChecker()
        return view
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startSosChecker() {
        handler.post(object : Runnable {
            override fun run() {
                // Pass logged-in user_id dynamically here
                checkSOS(userId = 1)
                handler.postDelayed(this, 5000)
            }
        })
    }

    private fun checkSOS(userId: Int) {
        val url = "$BASE_URL/get_sos.php?user_id=$userId"

        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                val status = json.optInt("status", 0)

                if (status == 1 && !isSosScreenOpen) {
                    isSosScreenOpen = true
                    val intent = Intent(requireContext(), GuardianSos::class.java)
                    startActivity(intent)
                } else if (status == 0) {
                    isSosScreenOpen = false
                }
            },
            { it.printStackTrace() }
        )

        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        // Reset flag so SOS can reopen if still ON
        isSosScreenOpen = false
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }
}
