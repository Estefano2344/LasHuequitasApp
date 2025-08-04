package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository
import com.proano.estefano.lashuequitasapp.model.entities.User

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val sessionManager = SessionManager(application)

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(emailOrUsername: String, password: String) {
        _isLoading.value = true

        if (!validarCampos(emailOrUsername, password)) {
            _isLoading.value = false
            return
        }

        val user = userRepository.authenticateUser(emailOrUsername.trim(), password)

        if (user != null) {
            sessionManager.createLoginSession(user)
            _loginResult.value = LoginResult.Success(user)
        } else {
            _loginResult.value = LoginResult.Error("Email/Usuario o contraseña incorrectos")
        }

        _isLoading.value = false
    }

    private fun validarCampos(emailOrUsername: String, password: String): Boolean {
        return when {
            emailOrUsername.trim().isEmpty() -> {
                _loginResult.value = LoginResult.Error("El email o usuario es obligatorio")
                false
            }
            password.isEmpty() -> {
                _loginResult.value = LoginResult.Error("La contraseña es obligatoria")
                false
            }
            else -> true
        }
    }

    fun isUserLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}