package com.example.foodyapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val mensajes: List<MensajeChat>,
    private val idUsuarioActual: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_MINE = 1
    private val TYPE_OTHER = 2

    override fun getItemViewType(position: Int): Int {
        val msg = mensajes[position]
        return if (msg.idRemitente == idUsuarioActual || msg.esMio) {
            TYPE_MINE
        } else {
            TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_MINE) {
            val view = inflater.inflate(R.layout.item_message_mine, parent, false)
            MineViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_message_other, parent, false)
            OtherViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = mensajes[position]
        if (holder is MineViewHolder) {
            holder.bind(msg)
        } else if (holder is OtherViewHolder) {
            holder.bind(msg)
        }
    }

    override fun getItemCount(): Int = mensajes.size

    inner class MineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMensaje: TextView = itemView.findViewById(R.id.txtMensajeMine)

        fun bind(m: MensajeChat) {
            txtMensaje.text = m.contenido
        }
    }

    inner class OtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtMensaje: TextView = itemView.findViewById(R.id.txtMensajeOther)
        private val imgAvatar: ImageView? = itemView.findViewById(R.id.imgAvatarOther)

        fun bind(m: MensajeChat) {
            txtMensaje.text = m.contenido
            // Aquí luego puedes cargar la foto del otro usuario con Glide si quieres
        }
    }
}
