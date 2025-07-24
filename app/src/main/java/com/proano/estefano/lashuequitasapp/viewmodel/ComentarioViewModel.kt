package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.proano.estefano.lashuequitasapp.model.businesslogic.ComentarioRepository
import com.proano.estefano.lashuequitasapp.model.entities.Comentario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ComentarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ComentarioRepository(application)

    private val _comentarios = MutableLiveData<List<Comentario>>()
    val comentarios: LiveData<List<Comentario>> = _comentarios

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _comentarioCreado = MutableLiveData<Boolean>()
    val comentarioCreado: LiveData<Boolean> = _comentarioCreado

    fun getComentariosByResenaId(resenaId: Long) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val comentariosList = repository.getComentariosByResenaId(resenaId)
                withContext(Dispatchers.Main) {
                    _comentarios.value = comentariosList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error al cargar comentarios: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun crearComentario(resenaId: Long, autorId: Long, contenido: String) {
        if (contenido.trim().isEmpty()) {
            _error.value = "El comentario no puede estar vacío"
            return
        }

        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val comentarioId = repository.crearComentario(resenaId, autorId, contenido.trim())
                withContext(Dispatchers.Main) {
                    if (comentarioId > 0) {
                        _comentarioCreado.value = true
                        // Recargar comentarios para mostrar el nuevo
                        getComentariosByResenaId(resenaId)
                    } else {
                        _error.value = "Error al crear el comentario"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error al crear comentario: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun eliminarComentario(comentarioId: Long, resenaId: Long) {
        _isLoading.value = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val eliminado = repository.eliminarComentario(comentarioId)
                withContext(Dispatchers.Main) {
                    if (eliminado) {
                        // Recargar comentarios después de eliminar
                        getComentariosByResenaId(resenaId)
                    } else {
                        _error.value = "Error al eliminar el comentario"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error al eliminar comentario: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetComentarioCreado() {
        _comentarioCreado.value = false
    }
}