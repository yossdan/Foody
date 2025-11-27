package com.example.foodyapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeedAdapter(private val lista: List<ModelRecetas>) :
    RecyclerView.Adapter<FeedAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAutor: ImageView = itemView.findViewById(R.id.imgAutor)
        val txtAutor: TextView  = itemView.findViewById(R.id.txtAutor)
        val imgReceta: ImageView = itemView.findViewById(R.id.imgReceta)
        val txtNombreReceta: TextView = itemView.findViewById(R.id.txtNombreReceta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_post, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receta = lista[position]

        holder.txtAutor.text = receta.autorNombre
        holder.txtNombreReceta.text = receta.nombre

        Glide.with(holder.itemView.context)
            .load(receta.imagen)
            .into(holder.imgReceta)

        receta.autorFoto?.takeIf { it.isNotBlank() }?.let { urlFoto ->
            Glide.with(holder.itemView.context)
                .load(urlFoto)
                .circleCrop()
                .into(holder.imgAutor)
        }

        // Click en autor → perfil público
        holder.txtAutor.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PerfilPublicoActivity::class.java)
            intent.putExtra("IdUsuarioPerfil", receta.autorId)
            context.startActivity(intent)
        }

        // Click en receta → detalle
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CrudRecetas::class.java)
            intent.putExtra("IdReceta", receta.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = lista.size
}
