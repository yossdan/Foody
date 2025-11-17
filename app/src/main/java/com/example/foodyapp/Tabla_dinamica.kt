package com.example.foodyapp

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Tabla_dinamica : AppCompatActivity() {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null
    private lateinit var sensorEventListener: SensorEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tabla_dinamica)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (sensor == null) {
            finish()
            return
        }

        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.values[0] < event.sensor.maximumRange) {
                    window.decorView.setBackgroundColor(Color.BLUE)
                } else {
                    window.decorView.setBackgroundColor(Color.CYAN)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
        inicio()
    }

    private fun inicio() {
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun detener() {
        sensorManager.unregisterListener(sensorEventListener)
    }

    override fun onPause() {
        super.onPause()
        detener()
    }

    override fun onResume() {
        super.onResume()
        inicio()
    }

}
