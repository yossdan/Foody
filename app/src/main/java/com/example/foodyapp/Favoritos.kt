package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@Suppress("DEPRECATION")
class Favoritos : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: FavoritosAdapter
    private var idUsuario: String = "" // Guardará el usuario logueado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        // ✅ 1. Primero obtenemos el IdUsuario que viene desde Login o MainActivity
        idUsuario = intent.getStringExtra("IdUsuario") ?: ""

        // 🔹 Si no existe IdUsuario, mostrar error
        if (idUsuario.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
        }

        // ✅ 2. Botón Atrás → Regresa a MainActivity enviando IdUsuario
        val btnAtras = findViewById<ImageButton>(R.id.btnAtrasMain)
        btnAtras.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("IdUsuario", idUsuario)
            startActivity(intent)
            finish()

            overridePendingTransition(
                android.R.anim.slide_in_left, // Slide-in suave de la actividad que queda
                android.R.anim.fade_out       // Fade-out de la actividad actual
            )
        }

        // ✅ 3. Configuración del RecyclerView
        recycler = findViewById(R.id.reciclajefavoritos)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = FavoritosAdapter(mutableListOf(), idUsuario)
        recycler.adapter = adapter

        // ✅ 4. Cargar los favoritos del usuario
        if (idUsuario.isNotEmpty()) {
            adapter.cargarFavoritos(this, idUsuario)
        }
    }
}
