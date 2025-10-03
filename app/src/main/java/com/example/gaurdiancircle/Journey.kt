package com.example.gaurdiancircle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import androidx.preference.PreferenceManager

class Journey : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay
    private lateinit var currentLocationTV: TextView
    private lateinit var destinationET: AutoCompleteTextView
    private lateinit var btnStart: Button
    private lateinit var btnArrived: Button   // ✅ Added for "Arrived Safely"
    private lateinit var backLayout: LinearLayout

    private val client = OkHttpClient()

    private var selectedDestLat: Double? = null
    private var selectedDestLon: Double? = null
    private var selectedDestName: String? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_journey)

        currentLocationTV = findViewById(R.id.currentLocationTV)
        destinationET = findViewById(R.id.destinationET)
        btnStart = findViewById(R.id.btn_start_journey)
        btnArrived = findViewById(R.id.btn_arrived)  // ✅ Initialize "Arrived Safely" button
        mapView = findViewById(R.id.liveMap)
        backLayout = findViewById(R.id.backk)

        mapView.setMultiTouchControls(true)

        // 🔙 Back header click → Go Home
        backLayout.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        // ✅ Arrived Safely button click → ChatPage
        btnArrived.setOnClickListener {
            val intent = Intent(this, ChatPage::class.java)
            intent.putExtra("auto_message", "I have arrived safely ✅")
            startActivity(intent)
        }

        // Location overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
        mapView.overlays.add(locationOverlay)

        // Ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else locationOverlay.enableMyLocation()

        // Move map & show address on first fix
        locationOverlay.runOnFirstFix {
            runOnUiThread {
                locationOverlay.myLocation?.let { loc ->
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(loc)
                    fetchAddress(loc.latitude, loc.longitude)
                }
            }
        }

        // Autocomplete search
        destinationET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim().orEmpty()
                if (query.length >= 2) {
                    fetchAutocompleteSuggestions(query) { suggestions ->
                        runOnUiThread {
                            val adapter = ArrayAdapter(
                                this@Journey,
                                android.R.layout.simple_dropdown_item_1line,
                                suggestions.map { it.first }
                            )
                            destinationET.setAdapter(adapter)
                            destinationET.showDropDown()

                            destinationET.setOnItemClickListener { _, _, pos, _ ->
                                val (name, coords) = suggestions[pos]
                                selectedDestName = name
                                selectedDestLat = coords.first
                                selectedDestLon = coords.second
                            }
                        }
                    }
                }
            }
        })

        // Start journey
        btnStart.setOnClickListener {
            val currentLoc = locationOverlay.myLocation
            if (currentLoc != null && selectedDestLat != null && selectedDestLon != null) {
                val intent = Intent(this, mapview::class.java).apply {
                    putExtra("startLat", currentLoc.latitude)
                    putExtra("startLon", currentLoc.longitude)
                    putExtra("currentAddress", currentLocationTV.text.toString())
                    putExtra("destLat", selectedDestLat!!)
                    putExtra("destLon", selectedDestLon!!)
                    putExtra("destName", selectedDestName ?: "Destination")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Select a destination", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Reverse geocoding
    private fun fetchAddress(lat: Double, lon: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&addressdetails=1"
            val request = Request.Builder().url(url).header("User-Agent", "GuardianCircleApp").build()
            val address = try {
                client.newCall(request).execute().use { response ->
                    response.body?.string()?.let { JSONObject(it).optString("display_name") }
                        ?: "Unknown location"
                }
            } catch (e: Exception) {
                "Unable to fetch address"
            }
            withContext(Dispatchers.Main) { currentLocationTV.text = address }
        }
    }

    // Autocomplete suggestions
    private fun fetchAutocompleteSuggestions(
        query: String,
        callback: (List<Pair<String, Pair<Double, Double>>>) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val url = "https://photon.komoot.io/api/?q=${query}&limit=5"
            val request = Request.Builder().url(url).header("User-Agent", "GuardianCircleApp").build()
            val suggestions = try {
                client.newCall(request).execute().use { response ->
                    response.body?.string()?.let { json ->
                        val list = mutableListOf<Pair<String, Pair<Double, Double>>>()
                        val features = JSONObject(json).optJSONArray("features")
                        if (features != null) {
                            for (i in 0 until features.length()) {
                                val feature = features.getJSONObject(i)
                                val props = feature.getJSONObject("properties")
                                val name = props.optString("name", "")
                                val city = props.optString("city", "")
                                val country = props.optString("country", "")
                                val fullName = listOf(name, city, country).filter { it.isNotEmpty() }
                                    .joinToString(", ")
                                val coords = feature.getJSONObject("geometry").getJSONArray("coordinates")
                                val lon = coords.getDouble(0)
                                val lat = coords.getDouble(1)
                                if (fullName.isNotEmpty())
                                    list.add(Pair(fullName, Pair(lat, lon)))
                            }
                        }
                        list
                    } ?: emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            callback(suggestions)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            locationOverlay.enableMyLocation()
        }
    }


    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDetach() }
}
