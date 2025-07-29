package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.ContentValues
import android.content.Context
import com.proano.estefano.lashuequitasapp.model.database.DatabaseHelper
import com.proano.estefano.lashuequitasapp.model.entities.User

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    fun insertUser(user: User): Boolean {
        val db = dbHelper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_NOMBRE, user.nombre)
                put(DatabaseHelper.COLUMN_APELLIDO, user.apellido)
                put(DatabaseHelper.COLUMN_EMAIL, user.email)
                put(DatabaseHelper.COLUMN_PASSWORD, user.password)
                put(DatabaseHelper.COLUMN_USUARIO, user.usuario)
                put(DatabaseHelper.COLUMN_PREFERENCIAS, user.preferenciasGastronomicas)
            }

            val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun isEmailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                arrayOf(DatabaseHelper.COLUMN_ID),
                "${DatabaseHelper.COLUMN_EMAIL} = ?",
                arrayOf(email),
                null, null, null
            )
            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    fun isUsernameExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                arrayOf(DatabaseHelper.COLUMN_ID),
                "${DatabaseHelper.COLUMN_USUARIO} = ?",
                arrayOf(username),
                null, null, null
            )
            val exists = cursor.count > 0
            cursor.close()
            exists
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }



    fun recuperarContrasena(email: String, nuevaContrasena: String, repetirContrasena: String): Pair<Boolean, String> {
        if (!isEmailExists(email)) {
            return Pair(false, "El correo no existe.")
        }
        if (nuevaContrasena != repetirContrasena) {
            return Pair(false, "Las contraseñas no coinciden.")
        }
        val db = dbHelper.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_PASSWORD, nuevaContrasena) // Ideal: hashear la contraseña
            }
            val rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                "${DatabaseHelper.COLUMN_EMAIL} = ?",
                arrayOf(email)
            )
            if (rows > 0) {
                Pair(true, "Contraseña actualizada correctamente.")
            } else {
                Pair(false, "No se pudo actualizar la contraseña.")
            }
        } catch (e: Exception) {
            Pair(false, "Error al actualizar la contraseña.")
        } finally {
            db.close()
        }
    }

    fun authenticateUser(emailOrUsername: String, password: String): User? {
        val db = dbHelper.readableDatabase
        return try {
            val cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null, // Seleccionar todas las columnas
                "${DatabaseHelper.COLUMN_EMAIL} = ? OR ${DatabaseHelper.COLUMN_USUARIO} = ?",
                arrayOf(emailOrUsername, emailOrUsername),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD))
                if (storedPassword == password) { // En producción, comparar hash de contraseñas
                    val user = User(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE)),
                        apellido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_APELLIDO)),
                        email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)),
                        password = storedPassword,
                        usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO)),
                        preferenciasGastronomicas = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PREFERENCIAS)) ?: ""
                    )
                    cursor.close()
                    user
                } else {
                    cursor.close()
                    null
                }
            } else {
                cursor.close()
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            db.close()
        }
    }
}