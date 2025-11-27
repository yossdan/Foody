package com.example.foodyapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ListaChatsActivity : AppCompatActivity() {

    private lateinit var adapter: ChatListAdapter
    private val lista = mutableListOf<ConversacionResumen>()
    private lateinit var idUsuarioActual: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_chats)

        val prefs = getSharedPreferences("FoodyPrefs", MODE_PRIVATE)
        idUsuarioActual = prefs.getString("IdUsuario", "") ?: ""

        if (idUsuarioActual.isEmpty()) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Botón atrás opcional
        findViewById<ImageButton?>(R.id.btnBackChats)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerConversaciones)
        adapter = ChatListAdapter(lista) { convo ->
            // Al dar click, abrimos ChatActivity usando tus mismos extras
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("IdUsuarioChat", convo.idUsuarioChat)
            intent.putExtra("NombreUsuarioChat", convo.nombreUsuario)
            intent.putExtra("FotoUsuarioChat", convo.fotoPerfil ?: "")
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        cargarConversaciones()
    }

    private fun cargarConversaciones() {
        val url = "http://10.0.2.2/foody/obtener_conversaciones_usuario.php?IdUsuario=$idUsuarioActual"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optString("status") == "success") {
                        val data = json.optJSONArray("data") ?: return@StringRequest
                        val temp = mutableListOf<ConversacionResumen>()

                        for (i in 0 until data.length()) {
                            val obj = data.getJSONObject(i)

                            val idConversacion = obj.optLong("IdConversacion")
                            val idUsuarioChat = obj.optInt("IdUsuarioChat").toString()
                            val nombreUsuario = obj.optString("NombreUsuario")
                            val fotoPerfil = obj.optString("FotoPerfil", null)
                            val ultimoMensaje = obj.optString("UltimoMensaje", "")
                            val fechaUltimo = obj.optString("FechaUltimoMensaje", "")

                            temp.add(
                                ConversacionResumen(
                                    idConversacion = idConversacion,
                                    idUsuarioChat = idUsuarioChat,
                                    nombreUsuario = nombreUsuario,
                                    fotoPerfil = fotoPerfil,
                                    ultimoMensaje = ultimoMensaje,
                                    fechaUltimoMensaje = fechaUltimo
                                )
                            )
                        }

                        adapter.actualizarLista(temp)
                    } else {
                        Toast.makeText(this, json.optString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}
