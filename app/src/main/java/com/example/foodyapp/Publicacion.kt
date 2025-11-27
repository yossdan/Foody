package com.example.foodyapp

data class Publicacion(
    val id: Long,
    val autorId: String,
    val autorNombre: String,
    val autorFoto: String?,
    val texto: String,
    val imagen: String?,      // null si no hay imagen
    val fecha: String,
    var totalLikes: Int,
    var totalComentarios: Int,
    var meGusta: Boolean
)
