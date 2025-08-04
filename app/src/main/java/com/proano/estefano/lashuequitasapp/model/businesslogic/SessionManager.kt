package com.proano.estefano.lashuequitasapp.model.businesslogic

import android.content.Context
import android.content.SharedPreferences
import com.proano.estefano.lashuequitasapp.model.entities.User

class SessionManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "LasHuequitasSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_FULL_NAME = "userFullName"
        private const val KEY_USER_PHOTO = "userPhoto"
    }

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = preferences.edit()

    fun createLoginSession(user: User) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, user.id)
        editor.putString(KEY_USER_NAME, user.usuario)
        editor.putString(KEY_USER_EMAIL, user.email)
        editor.putString(KEY_USER_FULL_NAME, "${user.nombre} ${user.apellido}")
        editor.putString(KEY_USER_PHOTO, user.foto)
        editor.apply()
    }

    fun updateUserPhoto(path: String?) {
        editor.putString(KEY_USER_PHOTO, path)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserName(): String? {
        return preferences.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return preferences.getString(KEY_USER_EMAIL, null)
    }

    fun getUserFullName(): String? {
        return preferences.getString(KEY_USER_FULL_NAME, null)
    }

    fun getUserId(): Long {
        return preferences.getLong(KEY_USER_ID, -1)
    }

    fun getUserPhotoPath(): String? {
        return preferences.getString(KEY_USER_PHOTO, null)
    }

    fun logout() {
        editor.clear()
        editor.apply()
    }
}