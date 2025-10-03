package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.gaurdiancircle.model.BasicResponse
import com.example.gaurdiancircle.retrofit.RetrofitClient
import com.example.gaurdiancircle.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var tvWelcomeName: TextView
    private lateinit var tvLocationSharing: TextView
    private lateinit var tvLastJourney: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize osmdroid config
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", 0)
        )

        mapView = view.findViewById(R.id.mapHome)
        mapView.setMultiTouchControls(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        tvWelcomeName = view.findViewById(R.id.tvWelcomeName)
        tvLocationSharing = view.findViewById(R.id.tvLocationSharing)

        val savedName = Utils.getUserName(requireContext())
        val userId = Utils.getUserId(requireContext())

        tvWelcomeName.text = "Hi ${savedName ?: "User"},"

        // Show device location on map if permission granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userPoint = GeoPoint(location.latitude, location.longitude)
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(userPoint)

                    val marker = Marker(mapView)
                    marker.position = userPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "You are here"
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                } else {
                    Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
        }

        setupNavigation(view, userId)
    }

    private fun setupNavigation(view: View, userId: Int) {
        view.findViewById<Button>(R.id.startJourneyButton).setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location == null) {
                    Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val lat = location.latitude
                val lng = location.longitude

                // Use logged in user ID from utils and fixed guardian email
                val sharedWithEmail = "vijay1@gmail.com"

                RetrofitClient.instance.shareLocation(userId, sharedWithEmail, lat, lng)
                    .enqueue(object : Callback<BasicResponse> {
                        override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                            if (response.isSuccessful && response.body()?.status == "success") {
                                Toast.makeText(requireContext(), "Location shared with The Guardians", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireContext(), livelocation::class.java)
                                intent.putExtra("shared_with", sharedWithEmail)
                                startActivity(intent)
                            } else {
                                val msg = response.body()?.message ?: "Server error"
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        view.findViewById<LinearLayout>(R.id.guardiansLayout).setOnClickListener {
            startActivity(Intent(requireContext(), Myguardians::class.java))
        }
        view.findViewById<LinearLayout>(R.id.chatt).setOnClickListener {
            val intent = Intent(requireContext(), ChatPage::class.java)
            startActivity(intent)
        }
        view.findViewById<LinearLayout>(R.id.silentSosLayout).setOnClickListener {
            startActivity(Intent(requireContext(), Sossetting::class.java))
        }
        view.findViewById<LinearLayout>(R.id.Journey).setOnClickListener {
            startActivity(Intent(requireContext(), Journey::class.java))
        }
        view.findViewById<Button>(R.id.emergencySosButton).setOnClickListener {
            startActivity(Intent(requireContext(), Sosscreen::class.java))
        }
        view.findViewById<ImageButton>(R.id.settings).setOnClickListener {
            startActivity(Intent(requireContext(), Settings::class.java))
        }


    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
