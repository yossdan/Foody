package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.ui.AppBarConfiguration
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    // Guardar los IDs y estados de las recetas recientes
    private val idsRecetas = mutableListOf<Int>()
    private val estadosFavorito = mutableListOf<Int>() // 0 o 1

    // Cola de Volley reutilizable
    private val queue by lazy { Volley.newRequestQueue(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.inicio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.Mensajes -> {
                    val intent = Intent(this, ListaChatsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.feed -> {
                    startActivity(Intent(this, FeedActivity::class.java))
                    finish()
                    true
                }
                R.id.inicio -> {
                    // Ir a la pantalla principal
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.perfil -> {
                    // Ir a tu pantalla de perfil (crea PerfilActivity si no la tienes)
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }


        val btnRecetas = findViewById<Button>(R.id.Recetas)
        btnRecetas.setOnClickListener {
            startActivity(Intent(this, Recetas::class.java))
        }

        val btnNuevaReceta: Button = findViewById(R.id.btnNuevareceta)
        val imageView = findViewById<ImageView>(R.id.imagenFotograma)
        val btnFavoritos: Button = findViewById(R.id.favoritos)

        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""

        // ------------------------------
        // FOTOGRAMAS (SLIDESHOW)
        // ------------------------------
        val imagenes = arrayOf(R.drawable.frame1, R.drawable.frame2, R.drawable.frame3)
        var index = 0
        val handler = Handler(Looper.getMainLooper())
        val duracionFade = 130L
        val duracionVisible = 3000L

        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = duracionFade }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = duracionFade }

        fun cambiarImagen() {
            imageView.setImageResource(imagenes[index])
            imageView.startAnimation(fadeIn)
            handler.postDelayed({ imageView.startAnimation(fadeOut) }, duracionVisible)
            handler.postDelayed({
                index = (index + 1) % imagenes.size
                cambiarImagen()
            }, duracionVisible + duracionFade)
        }

        cambiarImagen()

        // Cargar recetas recientes con favoritos
        cargarRecetasRecientes(idUsuario)

        btnNuevaReceta.setOnClickListener {
            startActivity(Intent(this, NuevaReceta::class.java))
        }

        btnFavoritos.setOnClickListener {
            val intent = Intent(this, Favoritos::class.java)
            intent.putExtra("IdUsuario", idUsuario)
            startActivity(intent)
        }
    }

    private fun cargarRecetasRecientes(idUsuario: String) {
        if (idUsuario.isBlank()) {
            println("IdUsuario vacío, no se pueden cargar recientes")
            return
        }

        val url = "http://10.0.2.2/foody/recientes.php?IdUsuario=$idUsuario"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                println("RESPONSE PHP: $response")

                val json = JSONObject(response)

                val status = json.optString("status", "error")
                if (status != "success") {
                    val message = json.optString("message", "Error desconocido")
                    println("ERROR PHP: $message")
                    return@StringRequest
                }

                val data = json.optJSONArray("data") ?: return@StringRequest
                if (data.length() == 0) {
                    println("No hay recetas para mostrar")
                    return@StringRequest
                }

                // Vistas de la UI
                val imagenesBtn = listOf(
                    findViewById<ImageButton>(R.id.Imagen1),
                    findViewById<ImageButton>(R.id.Imagen2),
                    findViewById<ImageButton>(R.id.Imagen3),
                    findViewById<ImageButton>(R.id.Imagen4)
                )

                val textos = listOf(
                    findViewById<TextView>(R.id.Producto1),
                    findViewById<TextView>(R.id.Producto2),
                    findViewById<TextView>(R.id.Producto3),
                    findViewById<TextView>(R.id.Producto4)
                )

                val favoritosBtn = listOf(
                    findViewById<MaterialButton>(R.id.favorito1),
                    findViewById<MaterialButton>(R.id.favorito2),
                    findViewById<MaterialButton>(R.id.favorito3),
                    findViewById<MaterialButton>(R.id.favorito4)
                )

                idsRecetas.clear()
                estadosFavorito.clear()

                // Máximo 4 items (los que caben en la UI)
                val maxItems = minOf(
                    data.length(),
                    imagenesBtn.size,
                    textos.size,
                    favoritosBtn.size
                )

                // Rellenar solo las tarjetas que tienen receta
                for (i in 0 until maxItems) {
                    val receta = data.getJSONObject(i)
                    val idReceta = receta.getInt("IdRecetas")
                    val nombre = receta.getString("NombreReceta")
                    val imagen = receta.getString("ImagenReceta")
                    val favorito = receta.getInt("Favorito")

                    idsRecetas.add(idReceta)
                    estadosFavorito.add(favorito)

                    // Texto
                    textos[i].text = nombre

                    // Imagen
                    Glide.with(this)
                        .load(imagen)
                        .into(imagenesBtn[i])

                    // Click en la imagen → detalle de esa receta
                    imagenesBtn[i].isEnabled = true
                    imagenesBtn[i].alpha = 1f
                    imagenesBtn[i].setOnClickListener {
                        val intent = Intent(this, CrudRecetas::class.java)
                        intent.putExtra("IdReceta", idReceta)
                        startActivity(intent)
                    }

                    // Estado inicial del favorito
                    favoritosBtn[i].icon = if (favorito == 1)
                        ContextCompat.getDrawable(this, R.drawable.corazon_lleno)
                    else
                        ContextCompat.getDrawable(this, R.drawable.heart_regular_full)

                    favoritosBtn[i].isEnabled = true
                    favoritosBtn[i].setOnClickListener {
                        val estaMarcado = estadosFavorito[i] == 1
                        cambiarEstadoFavorito(idUsuario, idReceta, !estaMarcado) {
                            estadosFavorito[i] = if (estaMarcado) 0 else 1
                            favoritosBtn[i].icon = if (estaMarcado)
                                ContextCompat.getDrawable(this, R.drawable.heart_regular_full)
                            else
                                ContextCompat.getDrawable(this, R.drawable.corazon_lleno)
                        }
                    }
                }

                // Limpiar / desactivar las tarjetas sobrantes
                for (i in maxItems until imagenesBtn.size) {
                    textos[i].text = ""
                    imagenesBtn[i].setImageDrawable(null)
                    imagenesBtn[i].isEnabled = false
                    imagenesBtn[i].alpha = 0f
                    imagenesBtn[i].setOnClickListener(null)

                    favoritosBtn[i].icon = null
                    favoritosBtn[i].isEnabled = false
                    favoritosBtn[i].setOnClickListener(null)
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

        queue.add(request)
    }

    private fun cambiarEstadoFavorito(
        idUsuario: String,
        idReceta: Int,
        marcar: Boolean,
        callback: () -> Unit
    ) {
        val url = "http://10.0.2.2/foody/recientes.php"
        val accion = if (marcar) "marcar" else "quitar"

        val req = object : StringRequest(
            Method.POST,
            url,
            { _ ->
                // Si el servidor responde OK, actualizamos UI vía callback
                callback()
            },
            { error ->
                error.printStackTrace()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "IdUsuario" to idUsuario,
                    "IdRecetas" to idReceta.toString(),
                    "accion" to accion
                )
            }
        }

        queue.add(req)
    }
}
