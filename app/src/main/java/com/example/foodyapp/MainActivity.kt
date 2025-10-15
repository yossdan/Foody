package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import com.example.foodyapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnNuevaReceta: Button = findViewById(R.id.btnNuevareceta)
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

        btnNuevaReceta.setOnClickListener {
            val intent = Intent(this, NuevaReceta::class.java)
            startActivity(intent)
        }
    }

}
