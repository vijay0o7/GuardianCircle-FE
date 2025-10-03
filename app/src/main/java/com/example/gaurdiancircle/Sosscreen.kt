package com.example.gaurdiancircle

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.gaurdiancircle.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class Sosscreen : AppCompatActivity() {

    private var vibrator: Vibrator? = null
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0
    private var userAddress: String = "Fetching location..."

    private lateinit var tvLocation: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvRecordingStatus: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sosscreen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Init views
        tvLocation = findViewById(R.id.tvLocation)
        tvTime = findViewById(R.id.timee)
        tvRecordingStatus = findViewById(R.id.tvRecordingStatus)

        // Show logged in user’s name on top
        val userName = Utils.getUserName(this) ?: "Unknown"
        tvRecordingStatus.text = "SOS ALERT • $userName"

        // Ask permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            101
        )

        // Get latest location
        fetchLastLocation()

        // Show time
        val currentTime = SimpleDateFormat("yyyy-MM-dd • HH:mm:ss", Locale.getDefault())
            .format(Date())
        tvTime.text = currentTime

        // ✅ Immediately send SOS ON with details
        sendSOS(userId = 1, status = 1)

        startSosAlarm()

        // Stop button
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopSosAlarm()
            sendSOS(userId = 1, status = 0) // SOS OFF
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        // Call police
        findViewById<Button>(R.id.btnCallPolice).setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:100")))
        }

        // Send to guardians
        findViewById<Button>(R.id.btnSendToGuardians).setOnClickListener {
            val intent = Intent(this, ChatPage::class.java)
            intent.putExtra("auto_message", "🚨 Emergency! I need help immediately.")
            startActivity(intent)
            finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startSosAlarm() {
        startVibration()

        // Auto stop after 30 sec
        Handler(Looper.getMainLooper()).postDelayed({
            stopSosAlarm()
        }, 30_000)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 1000, 1000)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopSosAlarm() {
        vibrator?.cancel()
    }

    private fun fetchLastLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val location: Location? =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                userLat = location.latitude
                userLng = location.longitude
                userAddress = getAddressFromLatLng(userLat, userLng)
                tvLocation.text = userAddress
            } else {
                tvLocation.text = "Location unavailable"
            }
        }
    }

    private fun getAddressFromLatLng(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Lat:$lat, Lng:$lng"
            } else {
                "Lat:$lat, Lng:$lng"
            }
        } catch (e: Exception) {
            "Lat:$lat, Lng:$lng"
        }
    }

    private fun sendSOS(userId: Int, status: Int) {
        val url = "https://jczx8mjg-80.inc1.devtunnels.ms/gaurdian_circle/update_sos.php"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response -> println("SOS API Response: $response") },
            { error -> error.printStackTrace() }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId.toString()
                params["status"] = status.toString()
                params["name"] = Utils.getUserName(this@Sosscreen) ?: "Unknown"
                params["phone"] = Utils.getUserPhone(this@Sosscreen) ?: "0000000000"
                params["lat"] = userLat.toString()
                params["lng"] = userLng.toString()
                params["location"] = userAddress
                return params
            }
        }
        Volley.newRequestQueue(this).add(stringRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSosAlarm()
        sendSOS(userId = 1, status = 0)
    }
}
