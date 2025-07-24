package com.proano.estefano.lashuequitasapp.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.viewmodel.ComentarioViewModel

class NuevoComentarioActivity : AppCompatActivity() {

    private lateinit var comentarioViewModel: ComentarioViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var editComentario: TextInputEditText
    private lateinit var btnSubir: Button

    private var resenaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_nuevo_comentario)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el ID de la reseña desde el Intent
        resenaId = intent.getLongExtra("resena_id", -1)

        if (resenaId == -1L) {
            Toast.makeText(this, "Error: No se pudo identificar la reseña", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupViewModel()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        editComentario = findViewById(R.id.editComentario)
        btnSubir = findViewById(R.id.btnSubir)
        sessionManager = SessionManager(this)
    }

    private fun setupViewModel() {
        comentarioViewModel = ViewModelProvider(this)[ComentarioViewModel::class.java]
    }

    private fun setupObservers() {
        // Observar si se está cargando
        comentarioViewModel.isLoading.observe(this) { isLoading ->
            btnSubir.isEnabled = !isLoading
            btnSubir.text = if (isLoading) "Enviando..." else getString(R.string.btn_subir)
        }

        // Observar errores
        comentarioViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                comentarioViewModel.clearError()
            }
        }

        // Observar cuando se crea el comentario
        comentarioViewModel.comentarioCreado.observe(this) { creado ->
            if (creado) {
                Toast.makeText(this, "Comentario agregado exitosamente", Toast.LENGTH_SHORT).show()
                comentarioViewModel.resetComentarioCreado()
                finish() // Cerrar la actividad y volver a la anterior
            }
        }
    }

    private fun setupListeners() {
        // Botón cerrar
        findViewById<ImageView>(R.id.closeComment).setOnClickListener {
            finish()
        }

        // Botón subir comentario
        btnSubir.setOnClickListener {
            enviarComentario()
        }
    }

    private fun enviarComentario() {
        val contenidoComentario = editComentario.text?.toString()?.trim()

        if (contenidoComentario.isNullOrEmpty()) {
            Toast.makeText(this, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar que el usuario esté logueado
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Debes iniciar sesión para comentar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            Toast.makeText(this, "Error: No se pudo identificar al usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el comentario
        comentarioViewModel.crearComentario(resenaId, userId, contenidoComentario)
    }
}