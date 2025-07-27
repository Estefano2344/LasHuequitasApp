package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.Context
import com.proano.estefano.lashuequitasapp.model.Favorito
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper

class FavoritosRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun obtenerFavoritos(userId: Long): List<Favorito> {
        val favoritos = mutableListOf<Favorito>()
        val db = dbHelper.readableDatabase
        val query = """
            SELECT r.nombre_restaurante, r.imagenes, r.calificacion, r.comentarios
            FROM favoritos f
            INNER JOIN resenas r ON f.resena_id = r.id
            WHERE f.user_id = ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        while (cursor.moveToNext()) {
            val nombre = cursor.getString(0)
            val imagenes = cursor.getString(1) // Ajusta según tu modelo Favorito
            val puntuacion = cursor.getDouble(2)
            val comentarios = cursor.getString(3)
            favoritos.add(Favorito(nombre, imagenes, puntuacion, comentarios))
        }
        cursor.close()
        db.close()
        return favoritos
    }

    fun agregarFavorito(userId: Long, resenaId: Long) {
        val db = dbHelper.writableDatabase
        val values = android.content.ContentValues().apply {
            put("user_id", userId)
            put("resena_id", resenaId)
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
}