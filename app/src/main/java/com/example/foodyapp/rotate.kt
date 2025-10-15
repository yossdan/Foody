package com.example.foodyapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Rotate : AppCompatActivity() {

    private lateinit var imagen: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rotate2)

        val imageView = findViewById<ImageView>(R.id.imagenFotograma)

        val imagenes = arrayOf(
            R.drawable.frame1,
            R.drawable.frame2,
            R.drawable.frame3
        )

        var index = 0
        val handler = Handler(Looper.getMainLooper())

        val duracionFade = 130L
        val duracionVisible = 3000L

        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = duracionFade }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = duracionFade }

        fun cambiarImagen() {
            imageView.setImageResource(imagenes[index])
            imageView.startAnimation(fadeIn)

            handler.postDelayed({
                imageView.startAnimation(fadeOut)
            }, duracionVisible)

            handler.postDelayed({
                index = (index + 1) % imagenes.size
                cambiarImagen()
            }, duracionVisible + duracionFade)
        }

        cambiarImagen()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mostrar)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

