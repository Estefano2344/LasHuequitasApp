// src/main/java/com/proano/estefano/lashuequitasapp/model/entities/Restaurant.kt
package com.proano.estefano.lashuequitasapp.model.entities

import java.io.Serializable

data class Restaurant(
    val id: Long = 0,
    val name: String,
    val imageUrl: String,
    val rating: Float,
    val reviewCount: Int,
) : Serializable