package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager // Asume que tienes un SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NuevaResenaViewModel(application: Application) : AndroidViewModel(application) {

    private val resenaRepository = ResenaRepository(application)
    private val sessionManager = SessionManager(application) // Asegúrate de tener esta clase

    private val _saveResult = MutableLiveData<SaveResenaResult>()
    val saveResult: LiveData<SaveResenaResult> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _imagesList = MutableLiveData<MutableList<Uri>>()
    val imagesList: LiveData<MutableList<Uri>> = _imagesList

    init {
        _imagesList.value = mutableListOf()
    }

    fun guardarResena(
        nombreRestaurante: String,
        ubicacion: String,
        rangoPrecio: String,
        tipoComida: String,
        calificacion: Float,
        tituloResena: String,
        comentarios: String,
        imageUris: List<Uri>
    ) {
        _isLoading.value = true

        // Validaciones
        if (!validarCampos(nombreRestaurante, ubicacion, tituloResena, calificacion)) {
            _isLoading.value = false
            return
        }

        // Obtener el ID del usuario logueado
        val autorId = sessionManager.getUserId()
        if (autorId == -1L) {
            _saveResult.value = SaveResenaResult.Error("Usuario no autenticado")
            _isLoading.value = false
            return
        }

        // Crear la reseña
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val resena = Resena(
            nombreRestaurante = nombreRestaurante.trim(),
            ubicacion = ubicacion.trim(),
            rangoPrecio = rangoPrecio,
            tipoComida = tipoComida,
            calificacion = calificacion,
            tituloResena = tituloResena.trim(),
            comentarios = comentarios.trim(),
            autorId = autorId,
            fechaCreacion = fechaActual
        )

        // Guardar en la base de datos en un hilo secundario
        viewModelScope.launch(Dispatchers.IO) {
            val success = resenaRepository.insertResena(resena, imageUris)
            _saveResult.postValue(
                if (success) {
                    SaveResenaResult.Success("Reseña guardada exitosamente")
                } else {
                    SaveResenaResult.Error("Error al guardar la reseña. Inténtalo de nuevo.")
                }
            )
            _isLoading.postValue(false)
        }
    }

    private fun validarCampos(
        nombreRestaurante: String,
        ubicacion: String,
        titulo: String,
        calificacion: Float
    ): Boolean {
        return when {
            nombreRestaurante.trim().isEmpty() -> {
                _saveResult.value = SaveResenaResult.Error("El nombre del restaurante es obligatorio")
                false
            }
            ubicacion.trim().isEmpty() -> {
                _saveResult.value = SaveResenaResult.Error("La ubicación es obligatoria")
                false
            }
            titulo.trim().isEmpty() -> {
                _saveResult.value = SaveResenaResult.Error("El título de la reseña es obligatorio")
                false
            }
            calificacion == 0f -> {
                _saveResult.value = SaveResenaResult.Error("La calificación es obligatoria")
                false
            }
            else -> true
        }
    }

    fun addImage(uri: Uri) {
        val currentList = _imagesList.value ?: mutableListOf()
        currentList.add(uri)
        _imagesList.value = currentList
    }

    fun removeImage(position: Int) {
        val currentList = _imagesList.value ?: mutableListOf()
        if (position >= 0 && position < currentList.size) {
            currentList.removeAt(position)
            _imagesList.value = currentList
        }
    }

    fun clearImages() {
        _imagesList.value = mutableListOf()
    }
}

// Clase sellada para manejar los resultados del guardado
sealed class SaveResenaResult {
    data class Success(val message: String) : SaveResenaResult()
    data class Error(val message: String) : SaveResenaResult()
}