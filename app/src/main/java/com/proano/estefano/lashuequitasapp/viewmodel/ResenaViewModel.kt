package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResenaViewModel(application: Application) : AndroidViewModel(application) {

    private val resenaRepository = ResenaRepository(application)

    // LiveData para las reseñas
    private val _resenas = MutableLiveData<List<Resena>>()
    val resenas: LiveData<List<Resena>> = _resenas

    // LiveData para estados de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Método para obtener reseñas por nombre de restaurante
    fun getResenasByRestauranteName(nombreRestaurante: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)

                val resenasObtenidas = resenaRepository.buscarResenasPorNombreRestaurante(nombreRestaurante)
                _resenas.postValue(resenasObtenidas)

            } catch (e: Exception) {
                _error.postValue("Error al cargar las reseñas: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Método para obtener todas las reseñas
    fun getAllResenas() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.postValue(true)
                _error.postValue(null)

                val todasLasResenas = resenaRepository.getAllResenas()
                _resenas.postValue(todasLasResenas)

            } catch (e: Exception) {
                _error.postValue("Error al cargar las reseñas: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    // Método para obtener reseña específica por ID
    fun getResenaById(resenaId: Long): LiveData<Resena?> {
        val resenaLiveData = MutableLiveData<Resena?>()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resena = resenaRepository.getResenaById(resenaId)
                resenaLiveData.postValue(resena)
            } catch (e: Exception) {
                _error.postValue("Error al cargar la reseña: ${e.message}")
                resenaLiveData.postValue(null)
            }
        }

        return resenaLiveData
    }

    // Método para limpiar errores
    fun clearError() {
        _error.value = null
    }
}