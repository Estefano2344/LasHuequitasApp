// src/main/java/com/proano/estefano/lashuequitasapp/model/database/DatabaseHelper.kt
package com.proano.estefano.lashuequitasapp.model.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lashuequitas.db"
        private const val DATABASE_VERSION = 7 // Increment to ensure onUpgrade is called

        // Tabla de usuarios
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_APELLIDO = "apellido"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_USUARIO = "usuario"
        const val COLUMN_PREFERENCIAS = "preferencias_gastronomicas"
        const val COLUMN_FOTO = "foto"

        // Tabla de reseñas
        const val TABLE_RESENAS = "resenas"
        const val COLUMN_RESENA_ID = "id"
        const val COLUMN_NOMBRE_RESTAURANTE = "nombre_restaurante"
        const val COLUMN_UBICACION = "ubicacion"
        const val COLUMN_RANGO_PRECIO = "rango_precio"
        const val COLUMN_TIPO_COMIDA = "tipo_comida" // Refers to the type of food in a review
        const val COLUMN_CALIFICACION = "calificacion"
        const val COLUMN_TITULO_RESENA = "titulo_resena"
        const val COLUMN_COMENTARIOS = "comentarios"
        const val COLUMN_AUTOR_ID = "autor_id"
        const val COLUMN_FECHA_CREACION = "fecha_creacion"
        const val COLUMN_IMAGENES = "imagenes" // Stores comma-separated image URLs (legacy)

        // Tabla de imágenes de reseñas (separate table for multiple images)
        const val TABLE_IMAGENES_RESENAS = "imagenes_resenas"
        const val COLUMN_IMAGEN_ID = "id"
        const val COLUMN_IMAGEN_RESENA_ID = "resena_id"
        const val COLUMN_RUTA_IMAGEN = "ruta_imagen"
        const val COLUMN_ORDEN = "orden"

        // Tabla de comentarios
        const val TABLE_COMENTARIOS = "comentarios"
        const val COLUMN_COMENTARIO_ID = "id"
        const val COLUMN_COMENTARIO_RESENA_ID = "resena_id"
        const val COLUMN_COMENTARIO_AUTOR_ID = "autor_id"
        const val COLUMN_COMENTARIO_CONTENIDO = "contenido"
        const val COLUMN_COMENTARIO_FECHA_CREACION = "fecha_creacion"

        // Tabla favoritos
        const val TABLE_FAVORITOS = "favoritos"
        const val COLUMN_FAVORITO_ID = "id"
        const val COLUMN_FAVORITO_USER_ID = "user_id"
        const val COLUMN_FAVORITO_RESTAURANT_ID = "restaurant_id"


        // Tabla para Restaurantes
        const val TABLE_RESTAURANTS = "restaurants"
        const val COLUMN_RESTAURANT_ID = "id"
        const val COLUMN_RESTAURANT_NAME = "name"
        const val COLUMN_RESTAURANT_IMAGE_URL = "image_url" // Path to the main image of the restaurant
        const val COLUMN_RESTAURANT_RATING = "rating"
        const val COLUMN_RESTAURANT_REVIEW_COUNT = "review_count"
        const val COLUMN_RESTAURANT_FOOD_TYPE = "food_type" // Added food_type column to restaurants table

    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
        CREATE TABLE $TABLE_USERS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOMBRE TEXT NOT NULL,
            $COLUMN_APELLIDO TEXT NOT NULL,
            $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
            $COLUMN_PASSWORD TEXT NOT NULL,
            $COLUMN_USUARIO TEXT NOT NULL UNIQUE,
            $COLUMN_PREFERENCIAS TEXT,
            $COLUMN_FOTO TEXT
        )
    """.trimIndent()

        val createResenasTable = """
        CREATE TABLE $TABLE_RESENAS (
            $COLUMN_RESENA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOMBRE_RESTAURANTE TEXT NOT NULL,
            $COLUMN_UBICACION TEXT NOT NULL,
            $COLUMN_RANGO_PRECIO TEXT NOT NULL,
            $COLUMN_TIPO_COMIDA TEXT NOT NULL,
            $COLUMN_CALIFICACION REAL NOT NULL,
            $COLUMN_TITULO_RESENA TEXT NOT NULL,
            $COLUMN_COMENTARIOS TEXT,
            $COLUMN_AUTOR_ID INTEGER NOT NULL,
            $COLUMN_FECHA_CREACION TEXT NOT NULL,
            $COLUMN_IMAGENES TEXT,
            FOREIGN KEY($COLUMN_AUTOR_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
        )
    """.trimIndent()

        val createImagenesResenasTable = """
        CREATE TABLE $TABLE_IMAGENES_RESENAS (
            $COLUMN_IMAGEN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGEN_RESENA_ID INTEGER NOT NULL,
            $COLUMN_RUTA_IMAGEN TEXT NOT NULL,
            $COLUMN_ORDEN INTEGER DEFAULT 0,
            FOREIGN KEY($COLUMN_IMAGEN_RESENA_ID) REFERENCES $TABLE_RESENAS($COLUMN_RESENA_ID) ON DELETE CASCADE
        )
    """.trimIndent()

        val createComentariosTable = """
        CREATE TABLE $TABLE_COMENTARIOS (
            $COLUMN_COMENTARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_COMENTARIO_RESENA_ID INTEGER NOT NULL,
            $COLUMN_COMENTARIO_AUTOR_ID INTEGER NOT NULL,
            $COLUMN_COMENTARIO_CONTENIDO TEXT NOT NULL,
            $COLUMN_COMENTARIO_FECHA_CREACION TEXT NOT NULL,
            FOREIGN KEY($COLUMN_COMENTARIO_RESENA_ID) REFERENCES $TABLE_RESENAS($COLUMN_RESENA_ID) ON DELETE CASCADE,
            FOREIGN KEY($COLUMN_COMENTARIO_AUTOR_ID) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
        )
    """.trimIndent()

        val createFavoritosTable = """
        CREATE TABLE $TABLE_FAVORITOS (
            $COLUMN_FAVORITO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_FAVORITO_USER_ID INTEGER NOT NULL,
            $COLUMN_FAVORITO_RESTAURANT_ID INTEGER NOT NULL,
            FOREIGN KEY($COLUMN_FAVORITO_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID),
            FOREIGN KEY($COLUMN_FAVORITO_RESTAURANT_ID) REFERENCES $TABLE_RESTAURANTS($COLUMN_RESTAURANT_ID)
        )
    """.trimIndent()

        val createRestaurantsTable = """
        CREATE TABLE $TABLE_RESTAURANTS (
            $COLUMN_RESTAURANT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_RESTAURANT_NAME TEXT NOT NULL,
            $COLUMN_RESTAURANT_IMAGE_URL TEXT NOT NULL,
            $COLUMN_RESTAURANT_RATING REAL NOT NULL,
            $COLUMN_RESTAURANT_REVIEW_COUNT INTEGER NOT NULL,
            $COLUMN_RESTAURANT_FOOD_TYPE TEXT NOT NULL DEFAULT ''
        )
    """.trimIndent()

        try {
            db.execSQL(createUsersTable)
            db.execSQL(createResenasTable)
            db.execSQL(createImagenesResenasTable)
            db.execSQL(createComentariosTable)
            db.execSQL(createFavoritosTable)
            db.execSQL(createRestaurantsTable)
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error al crear las tablas", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d("DatabaseHelper", "Upgrading database from version $oldVersion to $newVersion")
        // Elimina todas las tablas primero
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COMENTARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGENES_RESENAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESENAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESTAURANTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        // Crea todas las tablas de nuevo
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }
}