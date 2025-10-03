package com.example.gaurdiancircle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class Sossetting : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var shakeEnabled = true
    private var powerEnabled = true

    private var lastShakeTime: Long = 0
    private var shakeCount = 0

    private var powerCount = 0
    private var firstPowerTime: Long = 0

    private var screenReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sossetting)

        val shakeSwitch: Switch = findViewById(R.id.shakeSwitch)
        val powerSwitch: Switch = findViewById(R.id.powerSwitch)

        shakeSwitch.setOnCheckedChangeListener { _, isChecked -> shakeEnabled = isChecked }
        powerSwitch.setOnCheckedChangeListener { _, isChecked -> powerEnabled = isChecked }

        // Setup accelerometer
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Setup screen on/off listener for power button presses
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!powerEnabled) return

                if (intent?.action == Intent.ACTION_SCREEN_OFF || intent?.action == Intent.ACTION_SCREEN_ON) {
                    val now = SystemClock.elapsedRealtime()

                    if (firstPowerTime == 0L || now - firstPowerTime > 5000) {
                        // Reset if outside 5s window
                        firstPowerTime = now
                        powerCount = 1
                    } else {
                        powerCount++
                        if (powerCount >= 5) {
                            triggerSos()
                            resetPowerCounter()
                        }
                    }
                }
            }
        }

        // 🔙 Back button to home screen
        val backButton: ImageView = findViewById(R.id.Back)
        backButton.setOnClickListener {
            val intent = Intent(this, Home::class.java) // Replace Home with your actual Home activity
            startActivity(intent)
            finish()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!shakeEnabled || event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH

        if (acceleration > 12) { // Shake threshold
            val now = SystemClock.elapsedRealtime()

            if (lastShakeTime == 0L || now - lastShakeTime > 1000) {
                shakeCount = 1
                lastShakeTime = now
            } else {
                shakeCount++
                lastShakeTime = now

                if (shakeCount >= 3) {
                    triggerSos()
                    shakeCount = 0
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun triggerSos() {
        val intent = Intent(this, Sosscreen::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun resetPowerCounter() {
        powerCount = 0
        firstPowerTime = 0
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        // Register screenReceiver here instead of onCreate
        screenReceiver?.let {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            registerReceiver(it, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        screenReceiver?.let { unregisterReceiver(it) }
    }
}
