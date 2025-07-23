package com.proano.estefano.lashuequitasapp.model.entities

data class User(
    val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val usuario: String,
    val preferenciasGastronomicas: String // Las preferencias separadas por comas
)