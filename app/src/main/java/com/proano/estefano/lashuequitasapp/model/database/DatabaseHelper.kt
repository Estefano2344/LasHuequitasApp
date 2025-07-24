package com.proano.estefano.lashuequitasapp.model.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lashuequitas.db"
        private const val DATABASE_VERSION = 2

        // Tabla de usuarios
        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_NOMBRE = "nombre"
        const val COLUMN_APELLIDO = "apellido"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
        const val COLUMN_USUARIO = "usuario"
        const val COLUMN_PREFERENCIAS = "preferencias_gastronomicas"

        // Tabla de reseñas
        const val TABLE_RESENAS = "resenas"
        const val COLUMN_RESENA_ID = "id"
        const val COLUMN_NOMBRE_RESTAURANTE = "nombre_restaurante"
        const val COLUMN_UBICACION = "ubicacion"
        const val COLUMN_RANGO_PRECIO = "rango_precio"
        const val COLUMN_TIPO_COMIDA = "tipo_comida"
        const val COLUMN_CALIFICACION = "calificacion"
        const val COLUMN_TITULO_RESENA = "titulo_resena"
        const val COLUMN_COMENTARIOS = "comentarios"
        const val COLUMN_AUTOR_ID = "autor_id"
        const val COLUMN_FECHA_CREACION = "fecha_creacion"
        const val COLUMN_IMAGENES = "imagenes"

        // Tabla de imágenes de reseñas
        const val TABLE_IMAGENES_RESENAS = "imagenes_resenas"
        const val COLUMN_IMAGEN_ID = "id"
        const val COLUMN_IMAGEN_RESENA_ID = "resena_id"
        const val COLUMN_RUTA_IMAGEN = "ruta_imagen"
        const val COLUMN_ORDEN = "orden"
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
                $COLUMN_PREFERENCIAS TEXT
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
            FOREIGN KEY($COLUMN_AUTOR_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
        )
    """.trimIndent()

        val createImagenesResenasTable = """
        CREATE TABLE $TABLE_IMAGENES_RESENAS (
            $COLUMN_IMAGEN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_IMAGEN_RESENA_ID INTEGER NOT NULL,
            $COLUMN_RUTA_IMAGEN TEXT NOT NULL,
            $COLUMN_ORDEN INTEGER DEFAULT 0,
            FOREIGN KEY($COLUMN_IMAGEN_RESENA_ID) REFERENCES $TABLE_RESENAS($COLUMN_RESENA_ID)
        )
    """.trimIndent()
        // Ejecutar las sentencias SQL para crear las tablas
        try {
            db.execSQL(createUsersTable)
            db.execSQL(createResenasTable)
            db.execSQL(createImagenesResenasTable)
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Error al crear las tablas", e)
        }

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_RESENAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGENES_RESENAS")
        onCreate(db)
    }
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

}