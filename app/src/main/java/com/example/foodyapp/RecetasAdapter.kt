package com.example.foodyapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecetasAdapter(private val lista: List<ModelRecetas>) :
    RecyclerView.Adapter<RecetasAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imgReceta)
        val nombre: TextView = itemView.findViewById(R.id.txtNombreReceta)
        val autorNombre: TextView = itemView.findViewById(R.id.txtAutorReceta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recetas, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receta = lista[position]

        holder.nombre.text = receta.nombre
        holder.autorNombre.text = receta.autorNombre

        Glide.with(holder.itemView.context)
            .load(receta.imagen)
            .into(holder.imagen)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CrudRecetas::class.java)
            intent.putExtra("IdReceta", receta.id)
            context.startActivity(intent)
        }

        holder.autorNombre.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PerfilPublicoActivity::class.java)
            intent.putExtra("IdUsuarioPerfil", receta.autorId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = lista.size
}
