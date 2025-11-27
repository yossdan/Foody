package com.example.foodyapp

data class MensajeChat(
    val id: Long,
    val contenido: String,
    val idRemitente: String,
    val esMio: Boolean,
    val fecha: String
)
