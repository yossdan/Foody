package com.example.foodyapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.example.foodyapp.databinding.ActivityCrudRecetasBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.io.IOException

class CrudRecetas : AppCompatActivity() {

    private lateinit var binding: ActivityCrudRecetasBinding

    /** Id de la receta en BD (IdRecetas en PHP/MySQL) */
    private var idReceta: Int = -1

    /** Sabor actualmente seleccionado (se inicializa con el valor de la BD) */
    private var saborSeleccionado: String? = null

    /** Uri de la nueva imagen elegida por el usuario (si la cambia) */
    private var imagenUri: Uri? = null

    /** Cola de peticiones reutilizable para esta Activity */
    private val requestQueue by lazy { Volley.newRequestQueue(this) }

    /** Launcher moderno para seleccionar imagen (reemplaza startActivityForResult) */
    private val seleccionarImagenLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imagenUri = uri
                Glide.with(this)
                    .load(uri)
                    .fitCenter()
                    .into(binding.imagenFotograma2)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrudRecetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ Obtener el ID de la receta desde el Intent
        idReceta = obtenerIdRecetaDesdeIntent()
        if (idReceta == -1) {
            Toast.makeText(this, "No se encontró la receta seleccionada", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // 2️⃣ Configurar listeners de UI
        configurarBotones()
        configurarSelectorSabores()
        configurarBotonImagen()

        // 3️⃣ Cargar datos existentes desde el servidor
        cargarDatosDesdeServidor()
    }

    /**
     * Intenta obtener el IdReceta tanto como Int como String en el Intent.
     */
    private fun obtenerIdRecetaDesdeIntent(): Int {
        val idInt = intent.getIntExtra("IdReceta", -1)
        if (idInt != -1) return idInt

        val idString = intent.getStringExtra("IdReceta")
        return idString?.toIntOrNull() ?: -1
    }

    private fun configurarBotones() {
        // Botón atrás
        binding.btnAtrasActualizar.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()

            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out
            )
        }

        // Botón eliminar
        binding.btnEliminar.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Receta")
                .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Eliminar") { _, _ ->
                    eliminarReceta()
                }
                .show()
        }

        // Botón guardar (actualizar)
        binding.btnGuardar.setOnClickListener {
            if (!validarCamposObligatorios()) return@setOnClickListener

            if (imagenUri != null) {
                subirRecetaConImagen()
            } else {
                actualizarRecetaSinImagen()
            }
        }
    }

    private fun configurarSelectorSabores() {
        val sabores = resources.getStringArray(R.array.sabores_array)
        binding.saborPlatillo.setOnClickListener {
            mostrarDialogoSabores(sabores)
        }
    }

    private fun configurarBotonImagen() {
        binding.btnSubirImagen.setOnClickListener {
            seleccionarImagenLauncher.launch("image/*")
        }
    }

    private fun mostrarDialogoSabores(sabores: Array<String>) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Selecciona el sabor")
            .setItems(sabores) { _, which ->
                saborSeleccionado = sabores[which]
                binding.saborPlatillo.text = saborSeleccionado
            }
            .show()
    }

    /**
     * Carga la receta desde el servidor usando el idReceta.
     */
    private fun cargarDatosDesdeServidor() {
        val url = "$URL_OBTENER_RECETA?IdReceta=$idReceta"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response -> cargarDatosEnLaVista(response) },
            { error ->
                Toast.makeText(
                    this,
                    "Error al cargar la receta: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        requestQueue.add(request)
    }

    /**
     * Rellena la vista con los datos obtenidos en JSON.
     */
    private fun cargarDatosEnLaVista(json: String) {
        try {
            val obj = JSONObject(json)
            if (obj.getString("status") == "success") {
                val data = obj.getJSONObject("data")

                val nombre = data.getString("NombreReceta")
                val ingredientes = data.getString("Ingredientes")
                val tiempo = data.getString("TiempoPreparacion")
                val sabor = data.getString("SaborPlatillo")
                val pasos = data.getString("Pasos")
                val imagenUrl = data.optString("ImagenReceta", "")


                binding.editNombre.setText(nombre)
                binding.editIngredientes.setText(ingredientes)
                binding.tiempoPreparacion.setText(tiempo)
                binding.saborPlatillo.text = sabor
                binding.pasosEdit.setText(pasos)

                // Guardamos el sabor actual para que, si el usuario no lo cambia,
                // se envíe el mismo valor y no uno vacío.
                saborSeleccionado = sabor

                val esPublica = data.optInt("EsPublica", 0)
                binding.switchPublicarActualizar.isChecked = (esPublica == 1)


                if (imagenUrl.isNotBlank()) {
                    Glide.with(this)
                        .load(imagenUrl)
                        .fitCenter()
                        .into(binding.imagenFotograma2)
                }
            } else {
                Toast.makeText(this, obj.getString("message"), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar los datos de la receta", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Construye el mapa de parámetros comunes para actualizar la receta.
     */
    private fun construirParametrosBasicos(): MutableMap<String, String> {
        val nombre = binding.editNombre.text.toString().trim()
        val ingredientes = binding.editIngredientes.text.toString().trim()
        val tiempo = binding.tiempoPreparacion.text.toString().trim()
        val pasos = binding.pasosEdit.text.toString().trim()

        // Si el usuario no abrió el diálogo de sabor, usamos el texto actual del botón
        val sabor = (saborSeleccionado ?: binding.saborPlatillo.text?.toString())
            ?.trim()
            .orEmpty()

        // 🔥 LEER EL SWITCH "Publicar en el feed"
        val esPublica = if (binding.switchPublicarActualizar.isChecked) "1" else "0"

        return mutableMapOf(
            "IdRecetas"         to idReceta.toString(),
            "NombreReceta"      to nombre,
            "Ingredientes"      to ingredientes,
            "TiempoPreparacion" to tiempo,
            "SaborPlatillo"     to sabor,
            "Pasos"             to pasos,
            "EsPublica"         to esPublica    // 👈 NUEVO
        )
    }

    /**
     * Valida que los campos mínimos estén llenos antes de enviar al servidor.
     */
    private fun validarCamposObligatorios(): Boolean {
        val nombre = binding.editNombre.text.toString().trim()
        val ingredientes = binding.editIngredientes.text.toString().trim()
        val tiempo = binding.tiempoPreparacion.text.toString().trim()
        val pasos = binding.pasosEdit.text.toString().trim()

        if (nombre.isEmpty() || ingredientes.isEmpty() || tiempo.isEmpty() || pasos.isEmpty()) {
            Toast.makeText(
                this,
                "Por favor completa nombre, ingredientes, tiempo y pasos.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    /**
     * Actualiza la receta enviando también una nueva imagen en Base64.
     */
    private fun subirRecetaConImagen() {
        val params = construirParametrosBasicos()

        if (imagenUri == null) {
            actualizarRecetaSinImagen()
            return
        }

        val encodedImage: String = try {
            contentResolver.openInputStream(imagenUri!!)?.use { input ->
                val bytes = input.readBytes()
                // NO_WRAP para evitar saltos de línea innecesarios
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } ?: run {
                Toast.makeText(this, "No se pudo leer la imagen seleccionada", Toast.LENGTH_LONG)
                    .show()
                return
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Error al leer la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        params["ImagenReceta"] = encodedImage

        val request = object : StringRequest(
            Request.Method.POST,
            URL_ACTUALIZAR_RECETA,
            { response -> manejarRespuestaActualizacion(response) },
            { error ->
                Toast.makeText(
                    this,
                    "Error al actualizar la receta: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
        }

        requestQueue.add(request)
    }

    /**
     * Actualiza la receta sin cambiar la imagen.
     */
    private fun actualizarRecetaSinImagen() {
        val params = construirParametrosBasicos()

        val request = object : StringRequest(
            Request.Method.POST,
            URL_ACTUALIZAR_RECETA,
            { response -> manejarRespuestaActualizacion(response) },
            { error ->
                Toast.makeText(
                    this,
                    "Error al actualizar la receta: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
        }

        requestQueue.add(request)
    }

    /**
     * Maneja la respuesta JSON tanto para actualización con o sin imagen.
     */
    private fun manejarRespuestaActualizacion(response: String) {
        try {
            val obj = JSONObject(response)
            val message = obj.optString("message", "Operación completada")

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()

            if (obj.optString("status") == "success") {
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Respuesta inválida del servidor: $response", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Elimina la receta actual.
     */
    private fun eliminarReceta() {
        if (idReceta == -1) return

        val request = object : StringRequest(
            Request.Method.POST,
            URL_ELIMINAR_RECETA,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val status = obj.optString("status")
                    val message = obj.optString("message", "Operación completada")

                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

                    if (status == "success") {
                        finish()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Respuesta inválida del servidor al eliminar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error al eliminar la receta: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf("IdRecetas" to idReceta.toString())
        }

        requestQueue.add(request)
    }

    companion object {
        private const val BASE_URL = "http://10.0.2.2/foody/"
        private const val URL_OBTENER_RECETA = BASE_URL + "obtener_receta.php"
        private const val URL_ACTUALIZAR_RECETA = BASE_URL + "actualizar_receta.php"
        private const val URL_ELIMINAR_RECETA = BASE_URL + "eliminar_receta.php"
    }
}
