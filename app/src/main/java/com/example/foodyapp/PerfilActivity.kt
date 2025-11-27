package com.example.foodyapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class PerfilActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView

    private var fotoUri: Uri? = null

    private lateinit var txtNombre: TextView
    private lateinit var txtInfo: TextView

    private lateinit var txtSeguidores: TextView
    private lateinit var txtSeguidos: TextView
    private lateinit var txtRecetas: TextView
    private lateinit var txtFavoritos: TextView

    // Selector de imagen de perfil (galería)
    private val seleccionarFotoPerfil =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                fotoUri = uri
                imgAvatar.setImageURI(uri)   // preview inmediata
                subirFotoPerfil(uri)         // la mandamos al servidor
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // --- VIEWS ---
        imgAvatar    = findViewById(R.id.imgAvatar)
        txtNombre    = findViewById(R.id.txtNombreUsuario)
        txtInfo      = findViewById(R.id.txtInfoUsuario)

        txtSeguidores = findViewById(R.id.txtSeguidores)
        txtSeguidos   = findViewById(R.id.txtSeguidos)
        txtRecetas    = findViewById(R.id.txtRecetas)
        txtFavoritos  = findViewById(R.id.txtFavoritos)

        // Click en la foto de perfil → seleccionar imagen
        imgAvatar.setOnClickListener {
            seleccionarFotoPerfil.launch("image/*")
        }

        // SharedPreferences
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""

        Log.d("PerfilDebug", "IdUsuario en prefs = '$idUsuario'")

        // Cargar datos de BD
        if (idUsuario.isNotBlank()) {
            cargarDatosUsuario(idUsuario)
        } else {
            txtNombre.text = "Usuario Foody"
            txtInfo.text   = "No se encontró IdUsuario en preferencias"
        }

        // Botón EDITAR CORREO
        val btnEditarCorreo = findViewById<MaterialButton>(R.id.btnEditarCorreo)
        btnEditarCorreo.setOnClickListener {
            if (idUsuario.isBlank()) {
                Toast.makeText(this, "No se encontró IdUsuario", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDialogEditarCorreo(idUsuario)
            }
        }

        // Botón CERRAR SESIÓN
        val btnCerrarSesion = findViewById<MaterialButton>(R.id.btnCerrarSesion)
        btnCerrarSesion.setOnClickListener {
            prefs.edit().clear().apply()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.perfil

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
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.perfil -> {
                    true
                }
                else -> false
            }
        }

    }

    // ---------- Llamada a PHP para traer datos del usuario ----------
    private fun cargarDatosUsuario(idUsuario: String) {
        // idUsuario es a la vez perfil y viewer (es tu propio perfil)
        val url = "http://10.0.2.2/foody/obtener_perfil_usuario.php?IdUsuario=$idUsuario&IdViewer=$idUsuario"
        Log.d("PerfilDebug", "URL Perfil = $url")

        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Log.d("PerfilDebug", "Respuesta de PHP: $response")

                if (response.isNullOrBlank()) {
                    txtNombre.text = "Usuario Foody"
                    txtInfo.text   = "Respuesta vacía del servidor"
                    return@StringRequest
                }

                try {
                    val json = JSONObject(response)
                    if (json.optBoolean("success")) {
                        val usuarioJson = json.getJSONObject("usuario")

                        val nombre      = usuarioJson.optString("usuario", "Usuario Foody")
                        val correo      = usuarioJson.optString("correo", "Sin correo")
                        val seguidores  = usuarioJson.optInt("seguidores", 0)
                        val seguidos    = usuarioJson.optInt("seguidos", 0)
                        val recetas     = usuarioJson.optInt("recetas", 0)
                        val favoritos   = usuarioJson.optInt("favoritos", 0)
                        val fotoPerfil  = usuarioJson.optString("foto_perfil", null)

                        txtNombre.text = nombre
                        txtInfo.text   = correo

                        txtSeguidores.text = "$seguidores seguidores"
                        txtSeguidos.text   = "$seguidos seguidos"
                        txtRecetas.text    = "$recetas recetas"
                        txtFavoritos.text  = "$favoritos favoritos"

                        // Cargar foto de perfil si existe
                        if (!fotoPerfil.isNullOrBlank()) {
                            try {
                                var base64 = fotoPerfil.trim()

                                // Por si algún día viene "data:image/jpeg;base64,AAAA..."
                                val commaIndex = base64.indexOf(',')
                                if (commaIndex != -1) {
                                    base64 = base64.substring(commaIndex + 1)
                                }

                                val bytes = Base64.decode(base64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imgAvatar.setImageBitmap(bitmap)

                            } catch (e: Exception) {
                                e.printStackTrace()
                                imgAvatar.setImageResource(R.drawable.ic_profile)
                            }
                        } else {
                            imgAvatar.setImageResource(R.drawable.ic_profile)
                        }

                    } else {
                        val msg = json.optString("message", "Error al obtener usuario")
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        txtNombre.text = "Usuario Foody"
                        txtInfo.text   = msg
                        imgAvatar.setImageResource(R.drawable.ic_profile)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                    txtInfo.text = "Error al procesar JSON"
                    imgAvatar.setImageResource(R.drawable.ic_profile)
                }
            },
            { error ->
                error.printStackTrace()
                Log.e("PerfilDebug", "Error Volley: ${error.message}")
                Toast.makeText(this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                txtInfo.text = "Error de conexión con el servidor"
                imgAvatar.setImageResource(R.drawable.ic_profile)
            }
        )

        queue.add(request)
    }

    // ---------- Subir foto de perfil al servidor ----------
    private fun subirFotoPerfil(uri: Uri) {
        // Obtenemos IdUsuario de SharedPreferences (mismo que usas en otras pantallas)
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        val idUsuario = prefs.getString("IdUsuario", "") ?: ""

        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontró IdUsuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir la imagen a Base64
        val base64Foto: String = try {
            contentResolver.openInputStream(uri)?.use { input ->
                val bytes = input.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } ?: run {
                Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_LONG).show()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al leer la imagen: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        val url = "http://10.0.2.2/foody/actualizar_foto_perfil.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            StringRequest@{ response ->
                try {
                    Log.d("PerfilDebug", "Respuesta actualizar_foto: $response")

                    val raw = response ?: ""
                    val trimmed = raw.trim()

                    // 👉 Si NO empieza con { entonces NO es JSON → mostrar texto y salir
                    if (!trimmed.startsWith("{")) {
                        Toast.makeText(
                            this,
                            "Error en servidor:\n$trimmed",
                            Toast.LENGTH_LONG
                        ).show()
                        return@StringRequest
                    }

                    val json = JSONObject(trimmed)
                    val ok = json.optBoolean("success", false)
                    val msg = json.optString("message", "Error al actualizar foto")

                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                    if (ok) {
                        // Recargar datos del perfil
                        val prefs2 = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
                        val idUsuario2 = prefs2.getString("IdUsuario", "") ?: ""
                        if (idUsuario2.isNotBlank()) {
                            cargarDatosUsuario(idUsuario2)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "IdUsuario" to idUsuario,
                    "FotoPerfil" to base64Foto
                )
        }

        Volley.newRequestQueue(this).add(request)
    }

    // ---------- Llamada a PHP para actualizar SOLO el correo ----------
    private fun actualizarCorreo(idUsuario: String, nuevoCorreo: String) {
        val url = "http://10.0.2.2/foody/actualizar_usuario.php"

        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.POST,
            url,
            StringRequest@{ response ->
                Log.d("PerfilDebug", "Respuesta actualizar_correo: $response")

                if (response.isNullOrBlank()) {
                    Toast.makeText(this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }

                try {
                    val json = JSONObject(response)
                    val success = json.optBoolean("success", false)
                    val messages = json.optJSONArray("messages")

                    val msgList = mutableListOf<String>()
                    if (messages != null) {
                        for (i in 0 until messages.length()) {
                            msgList.add(messages.optString(i))
                        }
                    }

                    val mensajeFinal = if (msgList.isNotEmpty())
                        msgList.joinToString(" • ")
                    else if (success) "Correo actualizado correctamente"
                    else "Hubo un problema al actualizar el correo"

                    Toast.makeText(this, mensajeFinal, Toast.LENGTH_LONG).show()

                    if (success) {
                        // Volver a cargar los datos desde la BD
                        cargarDatosUsuario(idUsuario)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "IdUsuario" to idUsuario,
                    "correo" to nuevoCorreo   // solo correo, sin tocar password
                )
            }
        }

        queue.add(request)
    }

    // ---------- Diálogo para editar correo ----------
    private fun mostrarDialogEditarCorreo(idUsuario: String) {
        val editText = EditText(this).apply {
            hint = "Nuevo correo"
            setText(txtInfo.text)   // correo actual
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Cambiar correo")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoCorreo = editText.text.toString().trim()
                if (nuevoCorreo.isNotEmpty()) {
                    actualizarCorreo(idUsuario, nuevoCorreo)
                } else {
                    Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }
}
