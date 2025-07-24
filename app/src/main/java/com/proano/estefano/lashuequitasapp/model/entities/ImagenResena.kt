package com.proano.estefano.lashuequitasapp.model.entities

data class ImagenResena(
    val id: Long = 0,
    val resenaId: Long,
    val rutaImagen: String,
    val orden: Int = 0
)