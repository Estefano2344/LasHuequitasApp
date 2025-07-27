package com.proano.estefano.lashuequitasapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant // ADD THIS IMPORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // ADD THIS IMPORT
import java.text.SimpleDateFormat
import java.util.*

class NuevaResenaViewModel(application: Application) : AndroidViewModel(application) {

    private val resenaRepository = ResenaRepository(application)
    private val sessionManager = SessionManager(application)

    private val _saveResult = MutableLiveData<SaveResenaResult>()
    val saveResult: LiveData<SaveResenaResult> = _saveResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _imagesList = MutableLiveData<MutableList<Uri>>()
    val imagesList: LiveData<MutableList<Uri>> = _imagesList

    init {
        _imagesList.value = mutableListOf()
    }

    // This is the correct and unified guardarResena function
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
        // Assuming getUserId() is the correct method in your SessionManager
        val autorId = sessionManager.getUserId()
        if (autorId == -1L) {
            _saveResult.value = SaveResenaResult.Error("Usuario no autenticado. Por favor, inicia sesión de nuevo.")
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
            fechaCreacion = fechaActual,
            imagenes = "" // This field is typically for a list of image URLs/paths, but the main restaurant image is handled below
        )

        // Guardar en la base de datos en un hilo secundario
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insertar la reseña y obtener su ID.
                // Assuming insertResena returns the row ID (Long) or a boolean
                val resenaSavedSuccessfully = resenaRepository.insertResena(resena, imageUris)

                if (resenaSavedSuccessfully > 0) { // Check if insertResena returned a valid ID
                    // Logic to save/update the restaurant in the 'restaurants' table
                    val existingRestaurant = resenaRepository.getRestaurantByName(nombreRestaurante)

                    // Determine the image URL to save/update for the restaurant
                    val restaurantImageUrl: String = if (imageUris.isNotEmpty()) {
                        // Use the first image from the new review
                        imageUris.first().toString()
                    } else {
                        // If no new images, keep the existing one or use placeholder if new restaurant
                        existingRestaurant?.imageUrl ?: "placeholder_restaurant"
                    }

                    if (existingRestaurant == null) {
                        // If the restaurant doesn't exist, create it
                        val newRestaurant = Restaurant(
                            id = 0, // ID will be auto-assigned in the database
                            name = nombreRestaurante,
                            imageUrl = restaurantImageUrl, // Use the image from the review or placeholder
                            rating = calificacion, // First review sets the initial rating
                            reviewCount = 1
                        )
                        resenaRepository.insertRestaurant(newRestaurant)
                    } else {
                        // If the restaurant already exists, update its rating, review count, and image
                        val newReviewCount = existingRestaurant.reviewCount + 1
                        // Calculate the new weighted average
                        val newAverageRating = ((existingRestaurant.rating * existingRestaurant.reviewCount) + calificacion) / newReviewCount

                        val updatedRestaurant = existingRestaurant.copy(
                            rating = newAverageRating,
                            reviewCount = newReviewCount,
                            imageUrl = restaurantImageUrl // Update the restaurant's image URL
                        )
                        resenaRepository.updateRestaurant(updatedRestaurant)
                    }

                    // Notify success on the main thread
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResenaResult.Success("Reseña guardada exitosamente y restaurante actualizado.")
                    }
                } else {
                    // Notify error if the review itself wasn't saved
                    withContext(Dispatchers.Main) {
                        _saveResult.value = SaveResenaResult.Error("Error al guardar la reseña. Inténtalo de nuevo.")
                    }
                }
            } catch (e: Exception) {
                // Catch any exception during the saving process
                withContext(Dispatchers.Main) {
                    _saveResult.value = SaveResenaResult.Error("Error inesperado: ${e.localizedMessage ?: "Desconocido"}")
                }
                e.printStackTrace()
            } finally {
                // Ensure loading state is always turned off
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
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
                _saveResult.value = SaveResenaResult.Error("El nombre del restaurante es obligatorio.")
                false
            }
            ubicacion.trim().isEmpty() -> {
                _saveResult.value = SaveResenaResult.Error("La ubicación es obligatoria.")
                false
            }
            titulo.trim().isEmpty() -> {
                _saveResult.value = SaveResenaResult.Error("El título de la reseña es obligatorio.")
                false
            }
            calificacion == 0f -> {
                _saveResult.value = SaveResenaResult.Error("La calificación es obligatoria y debe ser mayor a 0.")
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