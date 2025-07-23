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
}