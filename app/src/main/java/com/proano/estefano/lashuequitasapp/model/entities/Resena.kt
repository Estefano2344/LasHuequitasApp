package com.proano.estefano.lashuequitasapp.model.entities

data class Resena(
    val id: Long = 0,
    val nombreRestaurante: String,
    val ubicacion: String,
    val rangoPrecio: String,
    val tipoComida: String,
    val calificacion: Float,
    val tituloResena: String,
    val comentarios: String,
    val autorId: Long,
    val fechaCreacion: String,
    val imagenes: String = ""
)