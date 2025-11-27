package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class PerfilPublicoActivity : AppCompatActivity() {

    private lateinit var imgAvatar: ImageView
    private lateinit var txtNombre: TextView
    private lateinit var txtInfo: TextView
    private lateinit var txtSeguidores: TextView
    private lateinit var txtSeguidos: TextView
    private lateinit var txtRecetas: TextView
    private lateinit var txtFavoritos: TextView
    private lateinit var btnSeguir: MaterialButton
    private lateinit var btnAtras: ImageView

    private var idPerfil: String = ""   // usuario que estoy viendo
    private var idViewer: String = ""   // yo (usuario logueado)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_publico)

        // Referencias UI
        imgAvatar     = findViewById(R.id.imgAvatarPublico)
        txtNombre     = findViewById(R.id.txtNombreUsuario)
        txtInfo       = findViewById(R.id.txtInfoUsuario)
        txtSeguidores = findViewById(R.id.txtSeguidores)
        txtSeguidos   = findViewById(R.id.txtSeguidos)
        txtRecetas    = findViewById(R.id.txtRecetas)
        txtFavoritos  = findViewById(R.id.txtFavoritos)
        btnSeguir     = findViewById(R.id.btnSeguir)
        btnAtras      = findViewById(R.id.btnAtrasPerfilPublico)

        // Id del perfil que voy a ver (viene desde el adapter)
        idPerfil = intent.getStringExtra("IdUsuarioPerfil") ?: ""

        // Id del usuario logueado (viewer)
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        idViewer = prefs.getString("IdUsuario", "") ?: ""

        // Botón atrás
        btnAtras.setOnClickListener {
            finish()
        }

        val btnMensaje = findViewById<MaterialButton>(R.id.btnMensaje)
        btnMensaje.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("IdUsuarioChat", idPerfil)   // 👈 aquí
            startActivity(intent)
        }





        // Validaciones básicas
        if (idPerfil.isBlank() || idViewer.isBlank()) {
            Toast.makeText(this, "Datos de usuario inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Si el usuario intenta ver su propio perfil, no tiene sentido seguirse
        if (idPerfil == idViewer) {
            btnSeguir.visibility = View.GONE
        }

        // Cargar datos del perfil
        cargarPerfilPublico()

        // Seguir / dejar de seguir
        btnSeguir.setOnClickListener {
            toggleSeguir()
        }
    }

    private fun cargarPerfilPublico() {
        val url = "http://10.0.2.2/foody/obtener_perfil_usuario.php?IdUsuario=$idPerfil&IdViewer=$idViewer"

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
                    if (!json.optBoolean("success", false)) {
                        val msg = json.optString("message", "Error al cargar perfil")
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                        return@StringRequest
                    }

                    val usuarioJson = json.getJSONObject("usuario")

                    val nombre       = usuarioJson.optString("usuario", "Usuario Foody")
                    val correo       = usuarioJson.optString("correo", "Sin correo")
                    val seguidores   = usuarioJson.optInt("seguidores", 0)
                    val seguidos     = usuarioJson.optInt("seguidos", 0)
                    val recetas      = usuarioJson.optInt("recetas", 0)
                    val favoritos    = usuarioJson.optInt("favoritos", 0)
                    val loSigo       = usuarioJson.optBoolean("lo_sigo", false)
                    val fotoPerfil   = usuarioJson.optString("foto_perfil", "")

                    txtNombre.text     = nombre
                    txtInfo.text       = correo
                    txtSeguidores.text = "$seguidores seguidores"
                    txtSeguidos.text   = "$seguidos seguidos"
                    txtRecetas.text    = "$recetas recetas"
                    txtFavoritos.text  = "$favoritos favoritos"

                    // Texto del botón según si ya lo sigo o no
                    if (idPerfil != idViewer) {
                        btnSeguir.text = if (loSigo) "Dejar de seguir" else "Seguir"
                        btnSeguir.visibility = View.VISIBLE
                    }

                    // Avatar (si tienes ruta completa en foto_perfil)
                    if (fotoPerfil.isNotBlank()) {
                        Glide.with(this)
                            .load(fotoPerfil)
                            .circleCrop()
                            .into(imgAvatar)
                    } else {
                        imgAvatar.setImageResource(R.drawable.ic_profile) // tu ícono por defecto
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar perfil", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    private fun toggleSeguir() {
        val url = "http://10.0.2.2/foody/toggle_seguir_usuario.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.POST,
            url,
            StringRequest@{ response ->
                if (response.isNullOrBlank()) {
                    Toast.makeText(this, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                    return@StringRequest
                }

                try {
                    val json = JSONObject(response)
                    val success    = json.optBoolean("success", false)
                    val accion     = json.optString("accion", "")
                    val seguidores = json.optInt("seguidores", 0)
                    val msg        = json.optString("message", "")

                    if (msg.isNotBlank()) {
                        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    }

                    if (success) {
                        // Actualizar contador y texto del botón
                        txtSeguidores.text = "$seguidores seguidores"
                        btnSeguir.text = if (accion == "follow") "Dejar de seguir" else "Seguir"
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
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "IdSeguidor" to idViewer,
                    "IdSeguido"  to idPerfil
                )
        }

        queue.add(request)
    }
}
