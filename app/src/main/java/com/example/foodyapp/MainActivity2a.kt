package com.example.foodyapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity2a : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_activity2a)

        // Ajuste de insets (barra de estado, navegación, etc.)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón "Acerca de"
        val btnAcerca = findViewById<Button>(R.id.BtnAcerca)
        btnAcerca.setOnClickListener {
            mostrarAcercaDePersonalizado()
        }
    }

    private fun mostrarAcercaDePersonalizado() {
        // Infla tu layout personalizado para el diálogo
        val vista = layoutInflater.inflate(R.layout.alert, null)

        val dialogo = AlertDialog.Builder(this)
            .setView(vista)
            .setCancelable(true)
            .setPositiveButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialogo.show()
    }
}
