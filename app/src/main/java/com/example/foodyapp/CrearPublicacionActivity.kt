package com.example.foodyapp

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class CrearPublicacionActivity : AppCompatActivity() {

    private lateinit var edtTexto: EditText
    private lateinit var imgPreview: ImageView
    private lateinit var btnImagen: Button
    private lateinit var btnPublicar: Button

    private var imagenUri: Uri? = null

    private val seleccionarImagen =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imagenUri = uri
                imgPreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_publicacion)

        edtTexto = findViewById(R.id.edtTexto)
        imgPreview = findViewById(R.id.imgPreview)
        btnImagen = findViewById(R.id.btnSeleccionarImagen)
        btnPublicar = findViewById(R.id.btnPublicar)

        btnImagen.setOnClickListener {
            seleccionarImagen.launch("image/*")
        }

        btnPublicar.setOnClickListener {
            publicar()
        }
    }

    private fun publicar() {
        val texto = edtTexto.text.toString().trim()
        if (texto.isEmpty() && imagenUri == null) {
            Toast.makeText(this, "Escribe algo o selecciona una imagen", Toast.LENGTH_LONG).show()
            return
        }

        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""
        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontró IdUsuario", Toast.LENGTH_SHORT).show()
            return
        }

        val params = hashMapOf(
            "IdUsuario" to idUsuario,
            "Texto" to texto
        )

        if (imagenUri != null) {
            try {
                val encoded = contentResolver.openInputStream(imagenUri!!)?.use { input ->
                    val bytes = input.readBytes()
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
                if (!encoded.isNullOrEmpty()) {
                    params["Imagen"] = encoded
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al leer la imagen", Toast.LENGTH_LONG).show()
                return
            }
        }

        val url = "http://10.0.2.2/foody/crear_publicacion.php"
        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    val json = org.json.JSONObject(response)
                    if (json.optString("status") == "success") {
                        Toast.makeText(this, "Publicación creada", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            json.optString("message", "Error al publicar"),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error de conexión: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
        }

        Volley.newRequestQueue(this).add(request)
    }
}
