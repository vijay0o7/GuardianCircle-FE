package com.example.gaurdiancircle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class GuardianSos : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserLocation: TextView
    private lateinit var tvSosTime: TextView
    private lateinit var btnNavigateToUser: Button
    private lateinit var btnCallUser: Button
    private lateinit var btnMarkResolved: Button

    private var userId: Int = 1 // Replace with logged-in user ID
    private var userPhone: String = ""
    private var userLat: Double = 0.0
    private var userLng: Double = 0.0

    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval: Long = 5000 // 5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_guardian_sos)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Init views
        tvUserName = findViewById(R.id.tvUserName)
        tvUserLocation = findViewById(R.id.tvUserLocation)
        tvSosTime = findViewById(R.id.tvSosTime)
        btnNavigateToUser = findViewById(R.id.btnNavigateToUser)
        btnCallUser = findViewById(R.id.btnCallUser)
        btnMarkResolved = findViewById(R.id.btnMarkResolvedd)

        // Start periodic fetch
        startFetching()

        // Navigate button
        btnNavigateToUser.setOnClickListener {
            if (userLat != 0.0 && userLng != 0.0) {
                val navUri = Uri.parse(
                    String.format(Locale.ENGLISH, "google.navigation:q=%f,%f&mode=d", userLat, userLng)
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, navUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }

        // Call user
        btnCallUser.setOnClickListener {
            if (userPhone.isNotEmpty()) {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:$userPhone")
                startActivity(dialIntent)
            }
        }

        // Mark resolved
        btnMarkResolved.setOnClickListener {
            markSosResolved(userId)
        }
    }

    private fun startFetching() {
        handler.post(object : Runnable {
            override fun run() {
                fetchSosInfo(userId)
                handler.postDelayed(this, refreshInterval)
            }
        })
    }

    private fun fetchSosInfo(userId: Int) {
        val url =
            "https://jczx8mjg-80.inc1.devtunnels.ms/gaurdian_circle/get_sos.php?user_id=$userId"

        val request = StringRequest(Request.Method.GET, url, { response ->
            try {
                val json = JSONObject(response)

                if (json.optBoolean("success", false)) {
                    // Update UI
                    tvUserName.text = "User: ${json.optString("name", "Unknown")}"
                    tvUserLocation.text = json.optString("location", "Location unavailable")
                    tvSosTime.text = json.optString("updated_at", "--")

                    // Save values for call & navigation
                    userLat = json.optDouble("lat", 0.0)
                    userLng = json.optDouble("lng", 0.0)
                    userPhone = json.optString("phone", "")

                    // Vibrate if active
                    if (json.optInt("status", 0) == 1) {
                        startVibration()
                    } else {
                        stopVibration()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, { error ->
            error.printStackTrace()
        })

        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    private fun markSosResolved(userId: Int) {
        val url = "https://jczx8mjg-80.inc1.devtunnels.ms/gaurdian_circle/update_sos.php"
        val request = object : StringRequest(Method.POST, url,
            { finish() }, // Close when resolved
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "user_id" to userId.toString(),
                    "status" to "0"
                )
            }
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 1000, 1000) // vibrate 1s, pause 1s
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibration()
        handler.removeCallbacksAndMessages(null)
    }
}
