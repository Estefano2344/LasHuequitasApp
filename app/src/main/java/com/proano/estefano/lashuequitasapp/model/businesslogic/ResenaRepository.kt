// src/main/java/com/proano/estefano/lashuequitasapp/model/businesslogic/ResenaRepository.kt
package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor // Agregado: Importación para Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log // Agregado: Importación para Log
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant // Importación ya existente
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ResenaRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // --- Existing Resena operations (keep them as they are) ---

    fun insertResena(resena: Resena, imageUris: List<Uri>): Long { // Cambiado a Long para devolver el ID
        val db = dbHelper.writableDatabase
        var resenaId: Long = -1L // Inicializar con un valor que indique fallo

        try {
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
                // Nota: COLUMN_IMAGENES en Resena es un String, si guardas URLs aquí, es diferente
                // a la tabla de IMAGENES_RESENAS. Asegúrate de la consistencia deseada.
            }

            resenaId = db.insert(DatabaseHelper.TABLE_RESENAS, null, resenaValues)

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
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
            db.close()
        }
        return resenaId
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

    fun buscarRestaurantsPorNombre(textoBusqueda: String): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESTAURANTS,
                null,
                "LOWER(${DatabaseHelper.COLUMN_RESTAURANT_NAME}) LIKE LOWER(?)", // Búsqueda parcial por nombre de restaurante
                arrayOf("%$textoBusqueda%"), // % permite buscar texto que contenga la palabra
                null, null,
                "${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC" // Puedes ordenar como prefieras
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val idIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_ID)
                    val nameIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                    val imageUrlIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                    val ratingIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                    val reviewCountIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)

                    if (idIndex != -1 && nameIndex != -1 && imageUrlIndex != -1 && ratingIndex != -1 && reviewCountIndex != -1) {
                        val id = it.getLong(idIndex)
                        val name = it.getString(nameIndex)
                        val imageUrl = it.getString(imageUrlIndex)
                        val rating = it.getFloat(ratingIndex)
                        val reviewCount = it.getInt(reviewCountIndex)
                        restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount))
                    } else {
                        Log.e("ResenaRepository", "One or more columns not found in buscarRestaurantsPorNombre")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return restaurants
    }

    fun insertRestaurant(restaurant: Restaurant): Long { // Cambiado a Long para devolver el ID
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_RESTAURANT_NAME, restaurant.name)
            put(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL, restaurant.imageUrl)
            put(DatabaseHelper.COLUMN_RESTAURANT_RATING, restaurant.rating)
            put(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT, restaurant.reviewCount)
        }
        val id = db.insert(DatabaseHelper.TABLE_RESTAURANTS, null, values)
        db.close()
        return id
    }

    fun updateRestaurant(restaurant: Restaurant): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_RESTAURANT_NAME, restaurant.name)
            put(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL, restaurant.imageUrl)
            put(DatabaseHelper.COLUMN_RESTAURANT_RATING, restaurant.rating)
            put(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT, restaurant.reviewCount)
        }
        val rowsAffected = db.update(
            DatabaseHelper.TABLE_RESTAURANTS,
            values,
            "${DatabaseHelper.COLUMN_RESTAURANT_ID} = ?",
            arrayOf(restaurant.id.toString())
        )
        db.close()
        return rowsAffected
    }

    fun getRestaurantByName(name: String): Restaurant? {
        val db = dbHelper.readableDatabase
        var restaurant: Restaurant? = null
        val cursor = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null,
            "${DatabaseHelper.COLUMN_RESTAURANT_NAME} = ?",
            arrayOf(name),
            null, null, null
        )
        cursor?.use {
            if (it.moveToFirst()) {
                // Es importante verificar que las columnas existan en el cursor
                val idIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)

                if (idIndex != -1 && nameIndex != -1 && imageUrlIndex != -1 && ratingIndex != -1 && reviewCountIndex != -1) {
                    val id = it.getLong(idIndex)
                    val restaurantName = it.getString(nameIndex)
                    val imageUrl = it.getString(imageUrlIndex)
                    val rating = it.getFloat(ratingIndex)
                    val reviewCount = it.getInt(reviewCountIndex)
                    restaurant = Restaurant(id, restaurantName, imageUrl, rating, reviewCount)
                } else {
                    Log.e("ResenaRepository", "One or more columns not found in getRestaurantByName")
                }
            }
        }
        cursor?.close()
        db.close()
        return restaurant
    }

    fun getRecommendedRestaurants(): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC, ${DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT} DESC", // Ordenar por calificación y luego por conteo de reseñas
            "5" // Limitar a 5 para "recomendados"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)

                if (idIndex != -1 && nameIndex != -1 && imageUrlIndex != -1 && ratingIndex != -1 && reviewCountIndex != -1) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val imageUrl = it.getString(imageUrlIndex)
                    val rating = it.getFloat(ratingIndex)
                    val reviewCount = it.getInt(reviewCountIndex)
                    restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount))
                } else {
                    Log.e("ResenaRepository", "One or more columns not found in getRecommendedRestaurants")
                }
            }
        }
        cursor?.close()
        db.close()
        return restaurants
    }

    fun getAllRestaurants(): List<Restaurant> { // Este método ya existía, pero se asegura su unicidad
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null, null, null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)

                if (idIndex != -1 && nameIndex != -1 && imageUrlIndex != -1 && ratingIndex != -1 && reviewCountIndex != -1) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val imageUrl = it.getString(imageUrlIndex)
                    val rating = it.getFloat(ratingIndex)
                    val reviewCount = it.getInt(reviewCountIndex)
                    restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount))
                } else {
                    Log.e("ResenaRepository", "One or more columns not found in getAllRestaurants")
                }
            }
        }
        cursor?.close()
        db.close()
        return restaurants
    }

    fun getPopularRestaurants(): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT} DESC, ${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC", // Ordenar por conteo de reseñas y luego por calificación
            "5" // Limitar a 5 para "populares"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndex(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)

                if (idIndex != -1 && nameIndex != -1 && imageUrlIndex != -1 && ratingIndex != -1 && reviewCountIndex != -1) {
                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val imageUrl = it.getString(imageUrlIndex)
                    val rating = it.getFloat(ratingIndex)
                    val reviewCount = it.getInt(reviewCountIndex)
                    restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount))
                } else {
                    Log.e("ResenaRepository", "One or more columns not found in getPopularRestaurants")
                }
            }
        }
        cursor?.close()
        db.close()
        return restaurants
    }
}