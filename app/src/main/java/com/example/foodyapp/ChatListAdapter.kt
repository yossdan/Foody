package com.example.foodyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class ChatListAdapter(
    private var conversaciones: MutableList<ConversacionResumen>,
    private val onClick: (ConversacionResumen) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: CircleImageView = itemView.findViewById(R.id.imgAvatarItemChat)
        val txtNombre: TextView = itemView.findViewById(R.id.txtNombreItemChat)
        val txtUltimoMensaje: TextView = itemView.findViewById(R.id.txtUltimoMensajeItemChat)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFechaItemChat)

        fun bind(c: ConversacionResumen) {
            txtNombre.text = c.nombreUsuario
            txtUltimoMensaje.text = if (c.ultimoMensaje.isNotEmpty()) c.ultimoMensaje else "Nuevo chat"
            txtFecha.text = c.fechaUltimoMensaje // luego lo formateas bonito

            if (!c.fotoPerfil.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(c.fotoPerfil)
                    .placeholder(R.drawable.user_solid_full)
                    .into(imgAvatar)
            } else {
                imgAvatar.setImageResource(R.drawable.user_solid_full)
            }

            itemView.setOnClickListener {
                onClick(c)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversacion, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversaciones[position])
    }

    override fun getItemCount(): Int = conversaciones.size

    fun actualizarLista(nuevaLista: List<ConversacionResumen>) {
        conversaciones.clear()
        conversaciones.addAll(nuevaLista)
        notifyDataSetChanged()
    }
}
