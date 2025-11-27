package com.example.foodyapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText

@Suppress("DEPRECATION")
class Recetas : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: RecetasAdapter

    // Lista completa que viene del servidor
    private val listaRecetasOriginal = mutableListOf<ModelRecetas>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recetas)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nombre_receta)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón atrás
        val btnBack = findViewById<ImageButton>(R.id.btnAtrasRecetas)
        btnBack.setOnClickListener { finish() }

        // Recycler
        recycler = findViewById(R.id.recyclerRecetas)
        recycler.layoutManager = GridLayoutManager(this, 2)
        adapter = RecetasAdapter(emptyList())
        recycler.adapter = adapter

        // Buscador
        val edtBuscar = findViewById<TextInputEditText>(R.id.edtBuscarReceta)
        edtBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarRecetas(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Cargar recetas desde PHP
        cargarRecetas()
    }

    private fun cargarRecetas() {
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""

        if (idUsuario.isEmpty()) {
            Log.e("RECETAS", "IdUsuario vacío, no se pueden cargar recetas")
            return
        }

        val url = "http://10.0.2.2/Foody/lista.php?IdUsuario=$idUsuario"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                val status = response.optString("status")
                val message = response.optString("message")
                Log.d("RECETAS", "Respuesta PHP: $status - $message")

                if (status == "success") {
                    val dataArray = response.optJSONArray("data") ?: return@JsonObjectRequest

                    listaRecetasOriginal.clear()

                    for (i in 0 until dataArray.length()) {
                        val item = dataArray.getJSONObject(i)

                        val id        = item.optString("IdRecetas")
                        val nombre    = item.optString("NombreReceta")
                        val imagen    = item.optString("ImagenReceta")
                        val autorId   = item.optString("AutorId")
                        val autorNom  = item.optString("AutorNombre")
                        val autorFoto = item.optString("FotoAutor", null)
                        val favorito  = item.optInt("Favorito", 0) == 1

                        listaRecetasOriginal.add(
                            ModelRecetas(
                                id          = id,
                                nombre      = nombre,
                                imagen      = imagen,
                                autorId     = autorId,
                                autorNombre = autorNom,
                                autorFoto   = autorFoto,
                                esFavorito  = favorito
                            )
                        )
                    }

                    adapter = RecetasAdapter(listaRecetasOriginal)
                    recycler.adapter = adapter


                    // Mostrar todo al inicio (sin filtro)
                    filtrarRecetas("")
                } else {
                    Log.e("RECETAS", "PHP_ERROR: $message")
                }
            },
            { error ->
                Log.e("RECETAS", "VOLLEY_ERROR: ${error.message ?: "Error desconocido"}")
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun filtrarRecetas(texto: String) {
        val listaFiltrada = if (texto.isBlank()) {
            listaRecetasOriginal
        } else {
            listaRecetasOriginal.filter {
                it.nombre.contains(texto, ignoreCase = true)
            }
        }

        adapter = RecetasAdapter(listaFiltrada)
        recycler.adapter = adapter
    }
}
