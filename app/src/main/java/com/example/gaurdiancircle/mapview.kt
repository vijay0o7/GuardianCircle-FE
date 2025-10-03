package com.example.gaurdiancircle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

class mapview : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var controller: IMapController
    private lateinit var estimatedTime: TextView
    private lateinit var btnSOS: Button

    private var startLat: Double = 0.0
    private var startLon: Double = 0.0
    private var destLat: Double = 0.0
    private var destLon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getSharedPreferences("osm_prefs", MODE_PRIVATE))
        setContentView(R.layout.activity_mapview)

        map = findViewById(R.id.mapView)
        estimatedTime = findViewById(R.id.estimatedTime)
        btnSOS = findViewById(R.id.btnSOS)

        // ✅ Navigate to SOS page
        btnSOS.setOnClickListener {
            val intent = Intent(this, Sosscreen::class.java)
            startActivity(intent)
        }

        // ✅ Get journey values from Intent
        startLat = intent.getDoubleExtra("startLat", 0.0)
        startLon = intent.getDoubleExtra("startLon", 0.0)
        destLat = intent.getDoubleExtra("destLat", 0.0)
        destLon = intent.getDoubleExtra("destLon", 0.0)

        // Setup map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        controller = map.controller
        controller.setZoom(14.5)
        controller.setCenter(GeoPoint(startLat, startLon))

        // Add markers
        addMarker(startLat, startLon, "Start")
        addMarker(destLat, destLon, "Destination")

        // Draw route
        drawRoute()
    }

    private fun addMarker(lat: Double, lon: Double, title: String) {
        val marker = Marker(map)
        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        map.overlays.add(marker)
    }

    private fun drawRoute() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(
                OkHttpClient.Builder().apply {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }.build()
            )
            .build()

        val service = retrofit.create(OpenRouteService::class.java)

        // ✅ API key must be in header
        val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImY1YzFlNzJmNjgwNzQ1NzE4MDI0YzNhNjc3YjlkNTUwIiwiaCI6Im11cm11cjY0In0="

        val call = service.getRoute(
            "driving-car",
            "$startLon,$startLat",
            "$destLon,$destLat",
            apiKey
        )

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonObject = JSONObject(response.body()!!)
                        val features = jsonObject.getJSONArray("features")

                        if (features.length() > 0) {
                            val geometry = features.getJSONObject(0).getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")

                            val polyline = Polyline()
                            polyline.width = 8f
                            polyline.setOnClickListener { _, _, _ -> true }

                            for (i in 0 until coordinates.length()) {
                                val coord = coordinates.getJSONArray(i)
                                val lon = coord.getDouble(0)
                                val lat = coord.getDouble(1)
                                polyline.addPoint(GeoPoint(lat, lon))
                            }

                            map.overlays.add(polyline)
                            map.invalidate()

                            // ✅ ETA
                            val properties = features.getJSONObject(0).getJSONObject("properties")
                            val summary = properties.getJSONObject("summary")
                            val durationSeconds = summary.getDouble("duration")
                            val durationMinutes = (durationSeconds / 60).toInt()

                            runOnUiThread {
                                estimatedTime.text = "ETA: $durationMinutes min"
                            }
                        } else {
                            estimatedTime.text = "No route found"
                        }
                    } catch (e: Exception) {
                        Log.e("MapViewActivity", "Parse error", e)
                        estimatedTime.text = "Error parsing route"
                    }
                } else {
                    Log.e("MapViewActivity", "API error: ${response.errorBody()?.string()}")
                    estimatedTime.text = "Route not found"
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("MapViewActivity", "Failure", t)
                estimatedTime.text = "Failed to connect"
            }
        })
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}

// ✅ Retrofit service interface
interface OpenRouteService {
    @GET("v2/directions/{profile}")
    fun getRoute(
        @Path("profile") profile: String,
        @Query("start") start: String,
        @Query("end") end: String,
        @Header("Authorization") apiKey: String
    ): Call<String>
}
