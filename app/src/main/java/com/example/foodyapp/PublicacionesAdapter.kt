package com.example.foodyapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import org.json.JSONObject
import android.util.Base64
import com.example.foodyapp.PerfilActivity
import com.example.foodyapp.PerfilPublicoActivity


class PublicacionesAdapter(
    private val publicaciones: MutableList<Publicacion>,
    private val idUsuarioActual: String
) : RecyclerView.Adapter<PublicacionesAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imgAutor: ImageView = v.findViewById(R.id.imgAutor)
        val txtAutor: TextView = v.findViewById(R.id.txtAutor)
        val txtFecha: TextView = v.findViewById(R.id.txtFecha)
        val txtTexto: TextView = v.findViewById(R.id.txtTexto)
        val imgContenido: ImageView = v.findViewById(R.id.imgContenido)
        val txtLikes: TextView = v.findViewById(R.id.txtLikes)
        val txtComentarios: TextView = v.findViewById(R.id.txtComentarios)
        val btnLike: TextView = v.findViewById(R.id.btnLike)
        val btnComment: TextView = v.findViewById(R.id.btnComment)
        val btnShare: TextView = v.findViewById(R.id.btnShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publicacion, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = publicaciones.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = publicaciones[position]
        val context = holder.itemView.context

        holder.txtAutor.text = p.autorNombre
        holder.txtFecha.text = p.fecha
        holder.txtTexto.text = p.texto
        holder.txtLikes.text = "${p.totalLikes} Me gusta"
        holder.txtComentarios.text = "${p.totalComentarios} comentarios"

        // Click en el nombre o foto del autor → abrir perfil
        holder.txtAutor.setOnClickListener {
            abrirPerfil(context, p.autorId)
        }

        holder.imgAutor.setOnClickListener {
            abrirPerfil(context, p.autorId)
        }


        // FOTO DEL AUTOR
        // IMAGEN DE LA PUBLICACIÓN (BASE64)
        if (!p.imagen.isNullOrEmpty()) {
            holder.imgContenido.visibility = View.VISIBLE

            try {
                var base64 = p.imagen.trim()

                // Por si algún día viniera así: "data:image/jpeg;base64,AAAA..."
                val commaIndex = base64.indexOf(',')
                if (commaIndex != -1) {
                    base64 = base64.substring(commaIndex + 1)
                }

                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                holder.imgContenido.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
                holder.imgContenido.visibility = View.GONE
            }
        } else {
            holder.imgContenido.visibility = View.GONE
        }


        // IMAGEN DE LA PUBLICACIÓN
// IMAGEN DE LA PUBLICACIÓN (VIENE EN BASE64)
        if (!p.imagen.isNullOrBlank()) {
            holder.imgContenido.visibility = View.VISIBLE

            try {
                var base64 = p.imagen.trim()

                // Por si algún día viene así: "data:image/jpeg;base64,AAAA..."
                val commaIndex = base64.indexOf(',')
                if (commaIndex != -1) {
                    base64 = base64.substring(commaIndex + 1)
                }

                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                holder.imgContenido.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
                holder.imgContenido.visibility = View.GONE
            }
        } else {
            holder.imgContenido.visibility = View.GONE
        }


        // ESTADO VISUAL LIKE
        holder.btnLike.text = if (p.meGusta) "Te gusta" else "Me gusta"

        holder.btnLike.setOnClickListener {
            toggleLike(context, p, holder)
        }

        holder.btnComment.setOnClickListener {
            Toast.makeText(context, "Abrir pantalla de comentarios (después la hacemos)", Toast.LENGTH_SHORT).show()
        }

        holder.btnShare.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "${p.autorNombre}: ${p.texto}")
            }
            context.startActivity(Intent.createChooser(sendIntent, "Compartir publicación"))
        }
    }

    private fun toggleLike(
        context: android.content.Context,
        p: Publicacion,
        holder: ViewHolder
    ) {
        val url = "http://10.0.2.2/foody/like_publicacion.php"

        val request = object : StringRequest(
            Method.POST,
            url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.optString("status") == "success") {
                        val meGusta = json.optInt("meGusta", 0) == 1
                        val total = json.optInt("totalLikes", p.totalLikes)

                        p.meGusta = meGusta
                        p.totalLikes = total

                        holder.btnLike.text = if (meGusta) "Te gusta" else "Me gusta"
                        holder.txtLikes.text = "$total Me gusta"
                    } else {
                        Toast.makeText(
                            context,
                            json.optString("message", "Error al cambiar like"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al procesar like", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(
                    context,
                    "Error de conexión: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> =
                hashMapOf(
                    "IdUsuario" to idUsuarioActual,
                    "IdPublicacion" to p.id.toString()
                )
        }

        Volley.newRequestQueue(context).add(request)
    }

    fun actualizarLista(nuevaLista: List<Publicacion>) {
        publicaciones.clear()
        publicaciones.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    private fun abrirPerfil(context: android.content.Context, autorId: String) {
        if (autorId == idUsuarioActual) {
            // Es mi propia publicación → voy a mi perfil
            val intent = Intent(context, PerfilActivity::class.java)
            context.startActivity(intent)
        } else {
            // Es otro usuario → perfil público
            val intent = Intent(context, PerfilPublicoActivity::class.java)
            intent.putExtra("IdUsuarioPerfil", autorId)
            context.startActivity(intent)
        }
    }

}
