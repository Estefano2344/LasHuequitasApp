package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.viewmodel.ResenaViewModel
import java.io.File
import android.graphics.BitmapFactory

class ReviewDetailsActivity : AppCompatActivity() {

    private lateinit var resenaViewModel: ResenaViewModel
    private lateinit var tvReviewTitle: TextView
    private lateinit var tvReviewDescription: TextView
    private lateinit var ivReviewImage: ImageView

    private var resenaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review_details)

        // Ajuste de inset para edge-to-edge sobre el root correcto
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.detail_root)
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupViewModel()
        setupListeners()

        // Obtener el ID de la reseña desde el Intent
        resenaId = intent.getLongExtra("resena_id", -1)

        if (resenaId != -1L) {
            loadReviewDetails()
        } else {
            Toast.makeText(this, "Error: No se pudo cargar la reseña", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        tvReviewTitle = findViewById(R.id.tvReviewTitle)
        tvReviewDescription = findViewById(R.id.tvReviewDescription)
        ivReviewImage = findViewById(R.id.ivReviewImage)
    }

    private fun setupViewModel() {
        resenaViewModel = ViewModelProvider(this)[ResenaViewModel::class.java]
    }

    private fun setupListeners() {
        // Cerrar Activity al pulsar la flecha de atrás
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // botón añadir comentario
        findViewById<ImageButton>(R.id.btnAddComment).setOnClickListener {
            val intent = Intent(this, NuevoComentarioActivity::class.java)
            intent.putExtra("resena_id", resenaId)
            startActivity(intent)
        }

        // Configuración del Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Navegar a la actividad de inicio (HomeActivity)
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                // Navegar a la pantalla de populares (PopularesActivity)
                R.id.nav_populares -> {
                    startActivity(Intent(this, PopularesActivity::class.java))
                    true
                }
                // Navegar a la pantalla para postear una reseña (NuevaResenaActivity)
                R.id.nav_postear -> {
                    startActivity(Intent(this, NuevaResenaActivity::class.java))
                    true
                }
                // Navegar a la pantalla de favoritos (FavoritosActivity)
                R.id.nav_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java))
                    true
                }
                // Navegar a la pantalla del perfil (PerfilActivity)
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadReviewDetails() {
        resenaViewModel.getResenaById(resenaId).observe(this) { resena ->
            resena?.let {
                displayReviewDetails(it)
            } ?: run {
                Toast.makeText(this, "No se pudo cargar la reseña", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Observar errores
        resenaViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                resenaViewModel.clearError()
            }
        }
    }

    private fun displayReviewDetails(resena: Resena) {
        tvReviewTitle.text = resena.tituloResena
        tvReviewDescription.text = resena.comentarios

        // Cargar la primera imagen si existe
        if (resena.imagenes.isNotEmpty()) {
            val imagePaths = resena.imagenes.split(",")
            if (imagePaths.isNotEmpty() && imagePaths[0].isNotEmpty()) {
                loadImageFromPath(imagePaths[0])
            }
        }
    }

    private fun loadImageFromPath(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let {
                    ivReviewImage.setImageBitmap(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Mantener la imagen por defecto si hay error
        }
    }
}