package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.Context
import com.proano.estefano.lashuequitasapp.model.Favorito
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper

class FavoritosRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun obtenerFavoritos(userId: Long): List<Favorito> {
        val favoritos = mutableListOf<Favorito>()
        val db = dbHelper.readableDatabase

        // Este repositorio es necesario para buscar la imagen de la última reseña
        val resenaRepository = ResenaRepository(context)

        val query = """
        SELECT r.name, r.image_url, r.rating, r.review_count
        FROM favoritos f
        INNER JOIN restaurants r ON f.restaurant_id = r.id
        WHERE f.user_id = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val defaultImagen = cursor.getString(1) // Imagen por defecto
            val puntuacion = cursor.getDouble(2)
            val comentarios = cursor.getString(3)

            // --- INICIO DE LA CORRECCIÓN ---
            // 1. Busca la imagen más reciente de una reseña para este restaurante.
            val imagenDeResena = resenaRepository.getLatestRestaurantImage(nombre)

            // 2. Decide qué imagen usar: la de la reseña si existe, o la de por defecto si no.
            val imagenFinal = if (!imagenDeResena.isNullOrEmpty()) {
                imagenDeResena
            } else {
                defaultImagen
            }
            // --- FIN DE LA CORRECCIÓN ---

            favoritos.add(Favorito(nombre, imagenFinal, puntuacion, comentarios))
        }
        cursor.close()
        db.close()
        return favoritos
    }

    fun agregarFavorito(userId: Long, restaurantId: Long) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("user_id", userId)
            put("restaurant_id", restaurantId)
        }
        db.insert("favoritos", null, values)
        db.close()
    }

    fun obtenerResenaIdPorNombre(nombreRestaurante: String): Long? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM resenas WHERE nombre_restaurante = ? LIMIT 1",
            arrayOf(nombreRestaurante)
        )
        val id = if (cursor.moveToFirst()) cursor.getLong(0) else null
        cursor.close()
        db.close()
        return id
    }
    fun estaEnFavoritos(userId: Long, restaurantId: Long): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT 1 FROM favoritos WHERE user_id = ? AND restaurant_id = ? LIMIT 1",
            arrayOf(userId.toString(), restaurantId.toString())
        )
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun eliminarDeFavoritos(userId: Long, restaurantId: Long) {
        val db = dbHelper.writableDatabase
        db.delete("favoritos", "user_id = ? AND restaurant_id = ?", arrayOf(userId.toString(), restaurantId.toString()))
        db.close()
    }
}