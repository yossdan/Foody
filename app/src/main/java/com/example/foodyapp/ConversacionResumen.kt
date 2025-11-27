package com.example.foodyapp

data class ConversacionResumen(
    val idConversacion: Long,
    val idUsuarioChat: String,
    val nombreUsuario: String,
    val fotoPerfil: String?,
    val ultimoMensaje: String,
    val fechaUltimoMensaje: String
)
