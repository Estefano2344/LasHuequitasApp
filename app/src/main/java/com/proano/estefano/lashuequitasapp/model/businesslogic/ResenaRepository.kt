// src/main/java/com/proano/estefano/lashuequitasapp/model/businesslogic/ResenaRepository.kt
package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import com.proano.estefano.lashuequitasapp.model.entities.User
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ResenaRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Método para obtener un usuario por ID (necesario para las preferencias)
    fun getUserById(userId: Long): User? {
        val db = dbHelper.readableDatabase
        var user: User? = null
        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val nombre = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE))
                val apellido = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APELLIDO))
                val email = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL))
                val password = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
                val usuario = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO))
                val preferencias = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREFERENCIAS)) ?: ""
                user = User(id, nombre, apellido, email, password, usuario, preferencias)
            }
        }
        cursor?.close()
        db.close()
        return user
    }

    // --- Operaciones de Reseña ---
    fun insertResena(resena: Resena, imageUris: List<Uri>): Long {
        val db = dbHelper.writableDatabase
        var resenaId: Long = -1L

        return try {
            db.beginTransaction()

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

            resenaId = db.insert(DatabaseHelper.TABLE_RESENAS, null, resenaValues)

            if (resenaId != -1L) {
                val imagesPaths = saveImages(imageUris, resenaId)

                imagesPaths.forEachIndexed { index, imagePath ->
                    val imageValues = ContentValues().apply {
                        put(DatabaseHelper.COLUMN_IMAGEN_RESENA_ID, resenaId)
                        put(DatabaseHelper.COLUMN_RUTA_IMAGEN, imagePath)
                        put(DatabaseHelper.COLUMN_ORDEN, index)
                    }
                    db.insert(DatabaseHelper.TABLE_IMAGENES_RESENAS, null, imageValues)
                }
                db.setTransactionSuccessful()
                resenaId
            } else {
                -1L
            }
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    private fun saveImages(imageUris: List<Uri>, resenaId: Long): List<String> {
        val imagePaths = mutableListOf<String>()

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

    // Agregar este método a la clase ResenaRepository existente

    // Método para obtener la imagen más reciente de un restaurante
    fun getLatestRestaurantImage(restaurantName: String): String? {
        val db = dbHelper.readableDatabase
        var imagePath: String? = null

        try {
            // Primero obtener la reseña más reciente del restaurante
            val resenaCursor = db.query(
                DatabaseHelper.TABLE_RESENAS,
                arrayOf(DatabaseHelper.COLUMN_RESENA_ID),
                "${DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE} = ?",
                arrayOf(restaurantName),
                null, null,
                "${DatabaseHelper.COLUMN_FECHA_CREACION} DESC",
                "1" // Limitar a 1 resultado
            )

            if (resenaCursor.moveToFirst()) {
                val resenaId = resenaCursor.getLong(
                    resenaCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID)
                )

                // Ahora obtener la primera imagen de esa reseña
                val imagenCursor = db.query(
                    DatabaseHelper.TABLE_IMAGENES_RESENAS,
                    arrayOf(DatabaseHelper.COLUMN_RUTA_IMAGEN),
                    "${DatabaseHelper.COLUMN_IMAGEN_RESENA_ID} = ?",
                    arrayOf(resenaId.toString()),
                    null, null,
                    "${DatabaseHelper.COLUMN_ORDEN} ASC",
                    "1" // Limitar a 1 resultado
                )

                if (imagenCursor.moveToFirst()) {
                    imagePath = imagenCursor.getString(
                        imagenCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RUTA_IMAGEN)
                    )
                }
                imagenCursor.close()
            }
            resenaCursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return imagePath
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
                "LOWER(${DatabaseHelper.COLUMN_NOMBRE_RESTAURANTE}) LIKE LOWER(?)",
                arrayOf("%$textoBusqueda%"),
                null, null,
                "${DatabaseHelper.COLUMN_FECHA_CREACION} DESC"
            )

            while (cursor.moveToNext()) {
                val resenaId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESENA_ID))
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

    // --- Operaciones de Restaurante ---
    fun insertRestaurant(restaurant: Restaurant): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_RESTAURANT_NAME, restaurant.name)
            put(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL, restaurant.imageUrl)
            put(DatabaseHelper.COLUMN_RESTAURANT_RATING, restaurant.rating)
            put(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT, restaurant.reviewCount)
            put(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE, restaurant.foodType)
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
            put(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE, restaurant.foodType)
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
                val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                val foodTypeIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                val id = it.getLong(idIndex)
                val restaurantName = it.getString(nameIndex)
                val imageUrl = it.getString(imageUrlIndex)
                val rating = it.getFloat(ratingIndex)
                val reviewCount = it.getInt(reviewCountIndex)
                val foodType = it.getString(foodTypeIndex)
                restaurant = Restaurant(id, restaurantName, imageUrl, rating, reviewCount, foodType)
            }
        }
        cursor?.close()
        db.close()
        return restaurant
    }

    fun getAllRestaurants(): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null, null, null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                val foodTypeIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val imageUrl = it.getString(imageUrlIndex)
                val rating = it.getFloat(ratingIndex)
                val reviewCount = it.getInt(reviewCountIndex)
                val foodType = it.getString(foodTypeIndex)
                restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount, foodType))
            }
        }
        cursor?.close()
        db.close()
        return restaurants
    }

    fun getRestaurantsByFoodTypes(foodTypes: List<String>): List<Restaurant> {
        if (foodTypes.isEmpty()) {
            return emptyList()
        }

        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase

        val placeholders = foodTypes.joinToString { "?" }
        val selection = "${DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE} IN ($placeholders)"
        val selectionArgs = foodTypes.toTypedArray()

        try {
            val cursor: Cursor? = db.query(
                DatabaseHelper.TABLE_RESTAURANTS,
                null,
                selection,
                selectionArgs,
                null, null,
                "${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC, ${DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT} DESC"
            )

            cursor?.use {
                while (it.moveToNext()) {
                    val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                    val nameIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                    val imageUrlIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                    val ratingIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                    val reviewCountIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                    val foodTypeIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                    val id = it.getLong(idIndex)
                    val name = it.getString(nameIndex)
                    val imageUrl = it.getString(imageUrlIndex)
                    val rating = it.getFloat(ratingIndex)
                    val reviewCount = it.getInt(reviewCountIndex)
                    val foodType = it.getString(foodTypeIndex)
                    restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount, foodType))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return restaurants
    }

    fun buscarRestaurantsPorNombre(textoBusqueda: String): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_RESTAURANTS,
                null,
                "LOWER(${DatabaseHelper.COLUMN_RESTAURANT_NAME}) LIKE LOWER(?)",
                arrayOf("%$textoBusqueda%"),
                null, null,
                "${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC"
            )

            while (cursor.moveToNext()) {
                val idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                val foodTypeIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                val id = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val imageUrl = cursor.getString(imageUrlIndex)
                val rating = cursor.getFloat(ratingIndex)
                val reviewCount = cursor.getInt(reviewCountIndex)
                val foodType = cursor.getString(foodTypeIndex)
                restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount, foodType))
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
        return restaurants
    }

    fun getRecommendedRestaurants(): List<Restaurant> {
        val restaurants = mutableListOf<Restaurant>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            DatabaseHelper.TABLE_RESTAURANTS,
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_RESTAURANT_RATING} DESC",
            "5"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                val foodTypeIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val imageUrl = it.getString(imageUrlIndex)
                val rating = it.getFloat(ratingIndex)
                val reviewCount = it.getInt(reviewCountIndex)
                val foodType = it.getString(foodTypeIndex)
                restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount, foodType))
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
            null, null, null, null, null,
            "${DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT} DESC",
            "5"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_ID)
                val nameIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_NAME)
                val imageUrlIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_IMAGE_URL)
                val ratingIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_RATING)
                val reviewCountIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_REVIEW_COUNT)
                val foodTypeIndex = it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RESTAURANT_FOOD_TYPE)

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val imageUrl = it.getString(imageUrlIndex)
                val rating = it.getFloat(ratingIndex)
                val reviewCount = it.getInt(reviewCountIndex)
                val foodType = it.getString(foodTypeIndex)
                restaurants.add(Restaurant(id, name, imageUrl, rating, reviewCount, foodType))
            }
        }
        cursor?.close()
        db.close()
        return restaurants
    }
}