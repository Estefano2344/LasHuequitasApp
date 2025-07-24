package com.proano.estefano.lashuequitasapp.model.entities

data class Comentario(
    val id: Long = 0,
    val resenaId: Long,
    val autorId: Long,
    val contenido: String,
    val fechaCreacion: String,
    val autorNombre: String = "",
    val autorUsuario: String = ""
)