package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository
import com.proano.estefano.lashuequitasapp.model.entities.User

class RegistroViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    private val _registroResult = MutableLiveData<RegistroResult>()
    val registroResult: LiveData<RegistroResult> = _registroResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun registrarUsuario(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        usuario: String,
        preferencias: List<String>
    ) {
        _isLoading.value = true

        if (!validarCampos(nombre, apellido, email, password, usuario)) {
            _isLoading.value = false
            return
        }

        if (userRepository.isEmailExists(email)) {
            _registroResult.value = RegistroResult.Error("El email ya está registrado")
            _isLoading.value = false
            return
        }

        if (userRepository.isUsernameExists(usuario)) {
            _registroResult.value = RegistroResult.Error("El nombre de usuario ya está en uso")
            _isLoading.value = false
            return
        }

        val user = User(
            nombre = nombre.trim(),
            apellido = apellido.trim(),
            email = email.trim().lowercase(),
            password = password,
            usuario = usuario.trim(),
            preferenciasGastronomicas = preferencias.joinToString(",")
        )

        val success = userRepository.insertUser(user)

        _registroResult.value = if (success) {
            RegistroResult.Success("Usuario registrado exitosamente")
        } else {
            RegistroResult.Error("Error al registrar el usuario. Inténtalo de nuevo.")
        }

        _isLoading.value = false
    }

    private fun validarCampos(
        nombre: String,
        apellido: String,
        email: String,
        password: String,
        usuario: String
    ): Boolean {
        return when {
            nombre.trim().isEmpty() -> {
                _registroResult.value = RegistroResult.Error("El nombre es obligatorio")
                false
            }
            apellido.trim().isEmpty() -> {
                _registroResult.value = RegistroResult.Error("El apellido es obligatorio")
                false
            }
            email.trim().isEmpty() -> {
                _registroResult.value = RegistroResult.Error("El email es obligatorio")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _registroResult.value = RegistroResult.Error("El formato del email no es válido")
                false
            }
            password.length < 6 -> {
                _registroResult.value = RegistroResult.Error("La contraseña debe tener al menos 6 caracteres")
                false
            }
            usuario.trim().isEmpty() -> {
                _registroResult.value = RegistroResult.Error("El nombre de usuario es obligatorio")
                false
            }
            usuario.trim().length < 3 -> {
                _registroResult.value = RegistroResult.Error("El nombre de usuario debe tener al menos 3 caracteres")
                false
            }
            else -> true
        }
    }
}

sealed class RegistroResult {
    data class Success(val message: String) : RegistroResult()
    data class Error(val message: String) : RegistroResult()
}