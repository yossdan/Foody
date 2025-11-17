package com.example.foodyapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.Volley
import com.example.foodyapp.databinding.ActivityNuevaRecetaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.io.ByteArrayOutputStream

@Suppress("DEPRECATION")
class NuevaReceta : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaRecetaBinding
    private var saborSeleccionado: String? = null
    private var imagenBytes: ByteArray? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult

                val input = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(input)

                if (bitmap != null) {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    imagenBytes = baos.toByteArray()

                    Toast.makeText(this, "Imagen seleccionada 👍", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_LONG).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNuevaRecetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sabores = resources.getStringArray(R.array.sabores_array)

        binding.btnAtras.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()

            overridePendingTransition(
                android.R.anim.slide_in_left, // Slide-in suave de la actividad que queda
                android.R.anim.fade_out       // Fade-out de la actividad actual
            )
        }

        binding.btnSeleccionarSabor.setOnClickListener {
            mostrarDialogoSabores(sabores)
        }

        binding.btnSubirImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            seleccionarImagen.launch(intent)
        }

        binding.btnGuardarReceta.setOnClickListener {
            guardarReceta()
        }
    }

    private fun guardarReceta() {

        if (imagenBytes == null) {
            Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_LONG).show()
            return
        }

        val nombre = binding.NombreReceta.text.toString().trim()
        val tiempo = binding.TiempoPreparacion.text.toString().trim()
        val ingrediente = binding.Ingredientes.text.toString().trim()
        val pasos = binding.Pasos.text.toString().trim()

        if (nombre.isEmpty() || tiempo.isEmpty() || ingrediente.isEmpty() || pasos.isEmpty() || saborSeleccionado.isNullOrEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        // 🔥 OBTENER ID USUARIO REAL DEL LOGIN
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", null)

        if (idUsuario == null) {
            Toast.makeText(this, "Error: no se encontró el usuario", Toast.LENGTH_LONG).show()
            return
        }

        // 🔥 ENVIAR EL ID REAL
        val params = mapOf(
            "nueva_receta" to nombre,
            "tiempo_preparacion" to tiempo,
            "sabor_platillo" to saborSeleccionado!!,
            "ingrediente" to ingrediente,
            "pasos" to pasos,
            "IdUsuario" to idUsuario      // ← YA NO ES "1"
        )


        val url = "http://10.0.2.2/Foody/insert.php"

        val request = MultipartRequest(
            url = url,
            params = params,
            fileKey = "imagen",
            fileData = imagenBytes!!,
            fileName = "foto.jpg",
            listener = { response ->
                val json = JSONObject(response)
                Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show()

                if (json.getString("status") == "success") {
                    finish()
                }
            },
            errorListener = {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun mostrarDialogoSabores(sabores: Array<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona el sabor")
            .setItems(sabores) { _, which ->
                saborSeleccionado = sabores[which]
                binding.btnSeleccionarSabor.text = saborSeleccionado
            }
            .show()
    }
}
