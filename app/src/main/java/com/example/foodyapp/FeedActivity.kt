package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject

class FeedActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: PublicacionesAdapter
    private val listaPublicaciones = mutableListOf<Publicacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feed)

        val txtQueEstasPensando = findViewById<TextView>(R.id.txtQueEstasPensando)
        txtQueEstasPensando.setOnClickListener {
            startActivity(Intent(this, CrearPublicacionActivity::class.java))
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainFeed)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recycler = findViewById(R.id.recyclerFeed)
        recycler.layoutManager = LinearLayoutManager(this)

        // Id del usuario logueado
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""

        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontró IdUsuario en sesión", Toast.LENGTH_SHORT).show()
        }

        adapter = PublicacionesAdapter(listaPublicaciones, idUsuario)
        recycler.adapter = adapter

        // Cargar publicaciones desde el servidor
        if (idUsuario.isNotBlank()) {
            cargarFeedPublicaciones(idUsuario)
        }

        // Bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.feed

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.feed -> true
                R.id.Mensajes -> {
                    val intent = Intent(this, ListaChatsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.inicio -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun cargarFeedPublicaciones(idUsuario: String) {
        val url = "http://10.0.2.2/foody/feed_publicaciones.php?IdUsuario=$idUsuario"
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                if (response.isNullOrBlank()) {
                    Toast.makeText(this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }

                try {
                    val json = JSONObject(response)
                    if (json.optString("status") == "success") {
                        val dataArray = json.optJSONArray("data")
                        val nuevas = mutableListOf<Publicacion>()

                        if (dataArray != null) {
                            for (i in 0 until dataArray.length()) {
                                val item = dataArray.getJSONObject(i)

                                val id = item.optLong("IdPublicacion")
                                val texto = item.optString("Texto")
                                val imagen = item.optString("Imagen", null)
                                val fecha = item.optString("Fecha")
                                val autorId = item.optInt("AutorId").toString()
                                val autorNombre = item.optString("AutorNombre")
                                val fotoAutor = item.optString("FotoAutor", null)
                                val totalLikes = item.optInt("TotalLikes", 0)
                                val totalComentarios = item.optInt("TotalComentarios", 0)
                                val meGusta = item.optInt("MeGusta", 0) == 1

                                nuevas.add(
                                    Publicacion(
                                        id = id,
                                        autorId = autorId,
                                        autorNombre = autorNombre,
                                        autorFoto = fotoAutor,
                                        texto = texto,
                                        imagen = imagen,
                                        fecha = fecha,
                                        totalLikes = totalLikes,
                                        totalComentarios = totalComentarios,
                                        meGusta = meGusta
                                    )
                                )
                            }
                        }

                        adapter.actualizarLista(nuevas)
                    } else {
                        val msg = json.optString("message", "Error al cargar publicaciones")
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }
}
