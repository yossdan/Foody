package com.example.foodyapp

data class Favorito(
    val id: String,
    val nombre: String,
    val imagen: String,
    val tiempo: String,
    var esFavorito: Boolean = true
)

