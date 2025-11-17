package com.example.foodyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecetasAdapter(private val lista: List<ModelRecetas>) :
    RecyclerView.Adapter<RecetasAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgReceta: ImageView = itemView.findViewById(R.id.imgReceta)
        val nombreReceta: TextView = itemView.findViewById(R.id.NombreReceta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recetas, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val receta = lista[position]

        // Asigna el nombre
        holder.nombreReceta.text = receta.nombre

        // Carga la imagen con Glide (desde URL)
        Glide.with(holder.itemView.context)
            .load(receta.imagen)
            .into(holder.imgReceta)

        holder.itemView.setOnClickListener {

            Toast.makeText(
                holder.itemView.context,
                "Seleccionaste: ${receta.nombre}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun getItemCount(): Int = lista.size
}
