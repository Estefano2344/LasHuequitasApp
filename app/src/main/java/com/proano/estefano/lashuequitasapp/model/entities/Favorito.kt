package com.proano.estefano.lashuequitasapp.model

data class Favorito(
    val nombre: String,
    val imagenes: String,      // Cambia a String si es texto
    val puntuacion: Double,
    val comentarios: String    // Cambia a String si es texto
)