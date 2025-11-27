package com.example.foodyapp

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Sensor : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var pelota: View

    private var posX = 0f
    private var posY = 0f
    private var maxX = 0f
    private var maxY = 0f

    private val factorMovimiento = 3.5f // ajusta para más o menos sensibilidad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sensor)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nombre_receta)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        pelota = findViewById(R.id.pelota)
        val frameLayout = findViewById<FrameLayout>(R.id.nombre_receta)

        // Esperamos a que el layout mida su tamaño real antes de centrar
        frameLayout.post {
            maxX = frameLayout.width - pelota.width.toFloat()
            maxY = frameLayout.height - pelota.height.toFloat()

            // Centramos la pelota al inicio
            posX = maxX / 2f
            posY = maxY / 2f
            pelota.translationX = posX
            pelota.translationY = posY
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            posX -= x * factorMovimiento
            posY += y * factorMovimiento

            posX = posX.coerceIn(0f, maxX)
            posY = posY.coerceIn(0f, maxY)

            pelota.translationX = posX
            pelota.translationY = posY
        }
    }
}
