package com.example.foodyapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
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

@Suppress("DEPRECATION")
class Recetas : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private val lista = ArrayList<ModelRecetas>()
    private lateinit var adapter: RecetasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recetas)

        findViewById<ImageButton>(R.id.btnAtrasRecetas).setOnClickListener {
            finish()
            overridePendingTransition(
                android.R.anim.slide_in_left, // Slide-in suave de la actividad que queda
                android.R.anim.fade_out       // Fade-out de la actividad actual
            )
        }





        // Padding automático
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView con GridLayout de 2 columnas
        recycler = findViewById(R.id.recyclerRecetas)
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Inicializar Adapter una vez
        adapter = RecetasAdapter(lista)
        recycler.adapter = adapter

        // 🔹 Obtener el ID del usuario logeado desde SharedPreferences
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "0")?.toInt() ?: 0

        if (idUsuario != 0) {
            cargarRecetasUsuario(idUsuario)
        } else {
            Log.e("USUARIO_ERROR", "No se encontró un usuario logeado")
        }
    }

    private fun cargarRecetasUsuario(idUsuario: Int) {
        val url = "http://10.0.2.2/foody/lista.php?IdUsuario=$idUsuario"
        Log.d("RECETAS_URL", url)

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                Log.d("RECETAS_JSON", response.toString())

                val status = response.optString("status", "error")
                val message = response.optString("message", "")
                if (status == "success") {
                    val data = response.getJSONArray("data")
                    Log.d("RECETAS_COUNT", "Cantidad de recetas: ${data.length()}")

                    lista.clear()
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        val receta = ModelRecetas(
                            obj.getString("IdRecetas"),
                            obj.getString("NombreReceta"),
                            obj.getString("ImagenReceta")
                        )
                        lista.add(receta)
                    }

                    adapter.notifyDataSetChanged()

                } else {
                    Log.e("PHP_ERROR", message)
                }

            },
            { error ->
                Log.e("VOLLEY_ERROR", error.message ?: "Error desconocido")
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
