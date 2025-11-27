package com.example.foodyapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var adapter: ChatAdapter
    private val mensajes = mutableListOf<MensajeChat>()

    private var idConversacion: Long = 0
    private lateinit var idUsuarioActual: String
    private lateinit var idUsuarioChat: String

    private var ultimoIdMensaje: Long = 0L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val nombreChat = intent.getStringExtra("NombreUsuarioChat") ?: ""
        val fotoChat = intent.getStringExtra("FotoUsuarioChat") ?: ""

        findViewById<TextView>(R.id.txtNombreChat).text = nombreChat

        val imgAvatar = findViewById<ImageView>(R.id.imgAvatarChat)
        if (fotoChat.isNotEmpty()) {
            Glide.with(this)
                .load(fotoChat)
                .placeholder(R.drawable.user_solid_full)
                .into(imgAvatar)
        } else {
            imgAvatar.setImageResource(R.drawable.user_solid_full)
        }

        val root = findViewById<View>(R.id.mainChat)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBars.top + 16,   // extra 16dp
                bottom = systemBars.bottom + 8
            )
            insets
        }

        // IDs
        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        idUsuarioActual = prefs.getString("IdUsuario", "") ?: ""
        idUsuarioChat = intent.getStringExtra("IdUsuarioChat") ?: ""

        if (idUsuarioActual.isEmpty() || idUsuarioChat.isEmpty()) {
            Toast.makeText(this, "Faltan datos para el chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Toolbar
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Recycler
        val recycler = findViewById<RecyclerView>(R.id.recyclerChat)
        adapter = ChatAdapter(mensajes, idUsuarioActual)
        recycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        recycler.adapter = adapter

        // Input
        val txtMensaje = findViewById<EditText>(R.id.txtMensaje)
        val btnEnviar = findViewById<ImageButton>(R.id.btnEnviar)
        btnEnviar.setOnClickListener {
            val texto = txtMensaje.text.toString().trim()
            if (texto.isNotEmpty() && idConversacion != 0L) {
                enviarMensaje(texto)
                txtMensaje.setText("")
            }
        }

        // Obtener o crear conversación
        obtenerOcrearConversacion()
    }

    private fun obtenerOcrearConversacion() {
        val url = "http://10.0.2.2/foody/obtener_o_crear_conversacion.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optString("status") == "success") {
                        idConversacion = json.optLong("IdConversacion")
                        iniciarPollingMensajes()
                    } else {
                        Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "IdUsuario1" to idUsuarioActual,
                    "IdUsuario2" to idUsuarioChat
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun iniciarPollingMensajes() {
        handler.post(object : Runnable {
            override fun run() {
                if (idConversacion != 0L) {
                    cargarMensajes()
                }
                handler.postDelayed(this, 3000) // cada 3 segundos
            }
        })
    }

    private fun cargarMensajes() {
        val url =
            "http://10.0.2.2/foody/obtener_mensajes.php?IdConversacion=$idConversacion&UltimoId=$ultimoIdMensaje"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optString("status") == "success") {
                        val data = json.optJSONArray("data") ?: return@StringRequest

                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)
                            val idMensaje = obj.optLong("IdMensaje")
                            val idRemitente = obj.optString("IdRemitente")
                            val contenido = obj.optString("Contenido")
                            val fecha = obj.optString("FechaEnvio")

                            val esMio = (idRemitente == idUsuarioActual)

                            mensajes.add(
                                MensajeChat(
                                    id = idMensaje,
                                    contenido = contenido,
                                    idRemitente = idRemitente,
                                    esMio = esMio,
                                    fecha = fecha
                                )
                            )

                            if (idMensaje > ultimoIdMensaje) {
                                ultimoIdMensaje = idMensaje
                            }
                        }

                        adapter.notifyDataSetChanged()
                        findViewById<RecyclerView>(R.id.recyclerChat)
                            .scrollToPosition(mensajes.size - 1)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun enviarMensaje(texto: String) {
        val url = "http://10.0.2.2/foody/enviar_mensaje.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            {
                // Opcional: podrías forzar un cargarMensajes() aquí
            },
            { error -> error.printStackTrace() }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf(
                    "IdConversacion" to idConversacion.toString(),
                    "IdRemitente" to idUsuarioActual,
                    "Contenido" to texto,
                    "Tipo" to "texto"
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
