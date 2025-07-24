package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ResenaRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertResena(resena: Resena, imageUris: List<Uri>): Boolean {
        val db = dbHelper.writableDatabase

        return try {
            db.beginTransaction()

            // Insertar la reseña
            val resenaValues = ContentValues().apply {
                put(DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE, resena.nombreRestaurante)
                put(DatabaseHelper.COLUMN_UBICACION, resena.ubicacion)
                put(DatabaseHelper.COLUMN_RANGO_PRECIO, resena.rangoPrecio)
                put(DatabaseHelper.COLUMN_TIPO_COMIDA, resena.tipoComida)
                put(DatabaseHelper.COLUMN_CALIFICACION, resena.calificacion)
                put(DatabaseHelper.COLUMN_TITULO_RESENA, resena.tituloResena)
                put(DatabaseHelper.COLUMN_COMENTARIOS, resena.comentarios)
                put(DatabaseHelper.COLUMN_AUTOR_ID, resena.autorId)
                put(DatabaseHelper.COLUMN_FECHA_CREACION, resena.fechaCreacion)


            }

            val resenaId = db.insert(DatabaseHelper.TABLE_RESENAS, null, resenaValues)

            if (resenaId != -1L) {
                // Guardar las imágenes en el almacenamiento interno de la app
                val imagesPaths = saveImages(imageUris, resenaId)

                // Insertar las rutas de imágenes en la tabla de imágenes
                imagesPaths.forEachIndexed { index, imagePath ->
                    val imageValues = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_IMAGEN_RESENA_ID, resenaId)
                        put(DatabaseHelper.COLUMN_RUTA_IMAGEN, imagePath)
                        put(DatabaseHelper.COLUMN_ORDEN, index)
                    }
                    db.insert(DatabaseHelper.TABLE_IMAGENES_RESENAS, null, imageValues)
                }

                db.setTransactionSuccessful()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun saveImages(imageUris: List<Uri>, resenaId: Long): List<String> {
        val imagePaths = mutableListOf<String>()

        // Crear directorio para las imágenes si no existe
        val imagesDir = File(context.filesDir, "resenas_images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        imageUris.forEachIndexed { index, uri ->
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                inputStream?.let { input ->
                    val fileName = "resena_${resenaId}_image_${index}_${System.currentTimeMillis()}.jpg"
                    val file = File(imagesDir, fileName)

                    val outputStream = FileOutputStream(file)
                    input.copyTo(outputStream)

                    outputStream.close()
                    input.close()

                    imagePaths.add(file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return imagePaths
    }

    fun getResenasByUserId(userId: Long): List<Resena> {
        val resenas = mutableListOf<Resena>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESENAS,
                null,
                "${DatabaseHelper.COLUMN_AUTOR_ID} = ?",
                arrayOf(userId.toString()),
                null, null,
                "${DatabaseHelper.COLUMN_FECHA_CREACION} DESC"
            )

            while (cursor.moveToNext()) {
                val resenaId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID))
                val imagenesRutas = getImagenesByResenaId(resenaId)
                val resena = Resena(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID)),
                    nombreRestaurante = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE)),
                    ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)),
                    rangoPrecio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RANGO_PRECIO)),
                    tipoComida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_COMIDA)),
                    calificacion = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION)),
                    tituloResena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITULO_RESENA)),
                    comentarios = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIOS)) ?: "",
                    autorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTOR_ID)),
                    fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_CREACION)),
                    imagenes = imagenesRutas.joinToString(",")
                )
                resenas.add(resena)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return resenas
    }

    fun getAllResenas(): List<Resena> {
        val resenas = mutableListOf<Resena>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESENAS,
                null, null, null, null, null,
                "${DatabaseHelper.COLUMN_FECHA_CREACION} DESC"
            )

            while (cursor.moveToNext()) {
                val resenaId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID))
                val imagenesRutas = getImagenesByResenaId(resenaId)
                val resena = Resena(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID)),
                    nombreRestaurante = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE)),
                    ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)),
                    rangoPrecio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RANGO_PRECIO)),
                    tipoComida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_COMIDA)),
                    calificacion = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION)),
                    tituloResena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITULO_RESENA)),
                    comentarios = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIOS)) ?: "",
                    autorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTOR_ID)),
                    fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_CREACION)),
                    imagenes = imagenesRutas.joinToString(",")
                )
                resenas.add(resena)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return resenas
    }

    fun getImagenesByResenaId(resenaId: Long): List<String> {
        val imagenes = mutableListOf<String>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_IMAGENES_RESENAS,
                arrayOf(DatabaseHelper.COLUMN_RUTA_IMAGEN),
                "${DatabaseHelper.COLUMN_IMAGEN_RESENA_ID} = ?",
                arrayOf(resenaId.toString()),
                null, null,
                "${DatabaseHelper.COLUMN_ORDEN} ASC"
            )

            while (cursor.moveToNext()) {
                val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RUTA_IMAGEN))
                imagenes.add(rutaImagen)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return imagenes
    }

    fun getResenaById(resenaId: Long): Resena? {
        val db = dbHelper.readableDatabase
        var resena: Resena? = null

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESENAS,
                null,
                "${DatabaseHelper.COLUMN_RESENA_ID} = ?",
                arrayOf(resenaId.toString()),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                // Obtener las imágenes de esta reseña
                val imagenesRutas = getImagenesByResenaId(resenaId)

                resena = Resena(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID)),
                    nombreRestaurante = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE)),
                    ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)),
                    rangoPrecio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RANGO_PRECIO)),
                    tipoComida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_COMIDA)),
                    calificacion = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION)),
                    tituloResena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITULO_RESENA)),
                    comentarios = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIOS)) ?: "",
                    autorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTOR_ID)),
                    fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_CREACION)),
                    imagenes = imagenesRutas.joinToString(",")
                )
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return resena
    }

    fun buscarResenasPorNombreRestaurante(textoBusqueda: String): List<Resena> {
        val resenas = mutableListOf<Resena>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESENAS,
                null,
                "LOWER(${DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE}) LIKE LOWER(?)", // Búsqueda parcial
                arrayOf("%$textoBusqueda%"), // % permite buscar texto que contenga la palabra
                null, null,
                "${DatabaseHelper.COLUMN_FECHA_CREACION} DESC"
            )

            while (cursor.moveToNext()) {
                val resenaId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID))

                // Obtener las imágenes de esta reseña
                val imagenesRutas = getImagenesByResenaId(resenaId)

                val resena = Resena(
                    id = resenaId,
                    nombreRestaurante = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE)),
                    ubicacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UBICACION)),
                    rangoPrecio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RANGO_PRECIO)),
                    tipoComida = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_COMIDA)),
                    calificacion = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CALIFICACION)),
                    tituloResena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITULO_RESENA)),
                    comentarios = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMENTARIOS)) ?: "",
                    autorId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTOR_ID)),
                    fechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA_CREACION)),
                    imagenes = imagenesRutas.joinToString(",")
                )
                resenas.add(resena)
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return resenas
    }
}