package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper
import com.proano.estefano.lashuequitasapp.model.entities.Comentario
import java.text.SimpleDateFormat
import java.util.*

class ComentarioRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context.applicationContext)

    fun insertarComentario(comentario: Comentario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_COMENTARIO_RESENA_ID, comentario.resenaId)
            put(DatabaseHelper.COLUMN_COMENTARIO_AUTOR_ID, comentario.autorId)
            put(DatabaseHelper.COLUMN_COMENTARIO_CONTENIDO, comentario.contenido)
            put(DatabaseHelper.COLUMN_COMENTARIO_FECHA_CREACION, comentario.fechaCreacion)
        }
        return db.insert(DatabaseHelper.TABLE_COMENTARIOS, null, values)
    }

    fun getComentariosByResenaId(resenaId: Long): List<Comentario> {
        val db = dbHelper.readableDatabase
        val comentarios = mutableListOf<Comentario>()

        val query = """
            SELECT c.${DatabaseHelper.COLUMN_COMENTARIO_ID},
                   c.${DatabaseHelper.COLUMN_COMENTARIO_RESENA_ID},
                   c.${DatabaseHelper.COLUMN_COMENTARIO_AUTOR_ID},
                   c.${DatabaseHelper.COLUMN_COMENTARIO_CONTENIDO},
                   c.${DatabaseHelper.COLUMN_COMENTARIO_FECHA_CREACION},
                   u.${DatabaseHelper.COLUMN_NOMBRE},
                   u.${DatabaseHelper.COLUMN_APELLIDO},
                   u.${DatabaseHelper.COLUMN_USUARIO}
            FROM ${DatabaseHelper.TABLE_COMENTARIOS} c
            INNER JOIN ${DatabaseHelper.TABLE_USERS} u 
                ON c.${DatabaseHelper.COLUMN_COMENTARIO_AUTOR_ID} = u.${DatabaseHelper.COLUMN_ID}
            WHERE c.${DatabaseHelper.COLUMN_COMENTARIO_RESENA_ID} = ?
            ORDER BY c.${DatabaseHelper.COLUMN_COMENTARIO_FECHA_CREACION} DESC
        """.trimIndent()

        val cursor: Cursor = db.rawQuery(query, arrayOf(resenaId.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                do {
                    val comentario = Comentario(
                        id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIO_ID)),
                        resenaId = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIO_RESENA_ID)),
                        autorId = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIO_AUTOR_ID)),
                        contenido = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIO_CONTENIDO)),
                        fechaCreacion = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIO_FECHA_CREACION)),
                        autorNombre = "${it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE))} ${it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APELLIDO))}",
                        autorUsuario = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO))
                    )
                    comentarios.add(comentario)
                } while (it.moveToNext())
            }
        }

        return comentarios
    }

    fun crearComentario(resenaId: Long, autorId: Long, contenido: String): Long {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val comentario = Comentario(
            resenaId = resenaId,
            autorId = autorId,
            contenido = contenido,
            fechaCreacion = fechaActual
        )

        return insertarComentario(comentario)
    }

    fun eliminarComentario(comentarioId: Long): Boolean {
        val db = dbHelper.writableDatabase
        val resultado = db.delete(
            DatabaseHelper.TABLE_COMENTARIOS,
            "${DatabaseHelper.COLUMN_COMENTARIO_ID} = ?",
            arrayOf(comentarioId.toString())
        )
        return resultado > 0
    }
}