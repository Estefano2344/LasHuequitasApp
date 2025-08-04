package com.proano.estefano.lashuequitasapp.model.entities

data class User(
    val id: Long = 0,
    var nombre: String,
    var apellido: String,
    var email: String,
    val password: String,
    var usuario: String,
    val preferenciasGastronomicas: String, // Las preferencias separadas por comas
    var foto: String? = null
)