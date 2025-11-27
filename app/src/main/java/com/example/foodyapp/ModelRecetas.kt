package com.example.foodyapp

data class ModelRecetas(
    val id: String,
    val nombre: String,
    val imagen: String,
    val autorId: String,
    val autorNombre: String,
    val autorFoto: String? = null,
    val esFavorito: Boolean = false
)
