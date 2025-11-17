package com.example.foodyapp

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import org.json.JSONObject

class FavoritosAdapter(
    private var listaFavoritos: MutableList<Favorito>,
    private val idUsuario: String // ✅ ID de usuario desde Login
) : RecyclerView.Adapter<FavoritosAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagenComida: ImageButton = itemView.findViewById(R.id.ImagenComida)
        val nombreComida: TextView = itemView.findViewById(R.id.NombreComida)
        val tiempo: TextView = itemView.findViewById(R.id.descripcion)
        val btnFavorito: MaterialButton = itemView.findViewById(R.id.favorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favoritos, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listaFavoritos[position]

        holder.nombreComida.text = item.nombre
        holder.tiempo.text = "Tiempo: ${item.tiempo} min"

        val urlImagen = "http://10.0.2.2/Foody/uploads/${item.imagen}"
        Log.d("FAVORITOS", "Imagen URL: $urlImagen")

        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .into(holder.imagenComida)

        // ❤️ Icono del corazón
        actualizarIconoFavorito(holder.btnFavorito, item.esFavorito, holder.itemView.context)

        // Click para cambiar favorito
        holder.btnFavorito.setOnClickListener {
            item.esFavorito = !item.esFavorito
            cambiarFavorito(holder.itemView.context, item.id, if (item.esFavorito) "add" else "remove")
            actualizarIconoFavorito(holder.btnFavorito, item.esFavorito, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int = listaFavoritos.size

    fun actualizarLista(nuevaLista: List<Favorito>) {
        listaFavoritos.clear()
        listaFavoritos.addAll(nuevaLista)
        notifyDataSetChanged()

        Log.d("FAVORITOS", "Lista actualizada con ${listaFavoritos.size} favoritos")
        listaFavoritos.forEach { Log.d("FAVORITOS", it.toString()) }
    }

    // 🔹 Actualiza el icono del botón según el estado
    private fun actualizarIconoFavorito(btn: MaterialButton, esFavorito: Boolean, context: Context) {
        btn.icon = ContextCompat.getDrawable(
            context,
            if (esFavorito) R.drawable.corazon_lleno else R.drawable.heart_regular_full
        )
    }

    // 🔹 CARGAR FAVORITOS DESDE PHP
    fun cargarFavoritos(context: Context, idUsuario: String) {
        val url = "http://10.0.2.2/Foody/favoritos.php"

        val request = object : StringRequest(
            Method.POST, url,
            { respuesta ->
                Log.d("FAVORITOS", "🔵 RESPUESTA PHP: $respuesta")
                try {
                    val json = JSONObject(respuesta)
                    if (json.getString("status") == "success") {
                        val dataArray = json.getJSONArray("data")
                        val lista = mutableListOf<Favorito>()

                        for (i in 0 until dataArray.length()) {
                            val item = dataArray.getJSONObject(i)
                            lista.add(
                                Favorito(
                                    id = item.getString("IdRecetas"),
                                    nombre = item.getString("NombreReceta"),
                                    tiempo = item.getString("TiempoPreparacion"),
                                    imagen = item.getString("ImagenReceta"),
                                    esFavorito = true
                                )
                            )
                        }
                        actualizarLista(lista)
                    } else {
                        Log.e("FAVORITOS", "Error PHP: ${json.getString("message")}")
                    }
                } catch (e: Exception) {
                    Log.e("FAVORITOS", "⚠️ ERROR JSON: ${e.message}")
                }
            },
            { error ->
                Log.e("FAVORITOS", "Error Volley: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "action" to "get",
                    "IdUsuario" to idUsuario
                )
        }

        Volley.newRequestQueue(context).add(request)
    }

    // 🔹 AGREGAR O REMOVER FAVORITO
    private fun cambiarFavorito(context: Context, idReceta: String, action: String) {
        val url = "http://10.0.2.2/Foody/favoritos.php"

        val request = object : StringRequest(
            Method.POST, url,
            { respuesta ->
                Toast.makeText(context, "Actualizado ✔", Toast.LENGTH_SHORT).show()
                Log.d("FAVORITOS", "Respuesta cambio favorito: $respuesta")
            },
            { error ->
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("FAVORITOS", "Error cambiar favorito: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "action" to action,
                    "IdUsuario" to idUsuario,
                    "IdRecetas" to idReceta
                )
        }

        Volley.newRequestQueue(context).add(request)
    }
}
