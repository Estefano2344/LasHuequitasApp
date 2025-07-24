// Archivo: RestaurantReviewsActivity.kt
package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.view.ReviewAdapter
import com.proano.estefano.lashuequitasapp.viewmodel.ResenaViewModel

class RestaurantReviewsActivity : AppCompatActivity() {

    private lateinit var resenaViewModel: ResenaViewModel
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutNoReviews: LinearLayout
    private var nombreRestaurante: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_reviews)

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el nombre del restaurante del Intent
        nombreRestaurante = intent.getStringExtra("RESTAURANT_NAME") ?: ""

        // Inicializar ViewModel
        resenaViewModel = ViewModelProvider(this)[ResenaViewModel::class.java]

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewReviews)
        progressBar = findViewById(R.id.progressBar)
        layoutNoReviews = findViewById(R.id.layoutNoReviews)

        // Configurar RecyclerView
        setupRecyclerView()

        // Configurar UI (incluyendo la barra superior y navegación inferior)
        setupUI()

        // Observar los datos del ViewModel
        observeViewModel()

        // Cargar las reseñas del restaurante
        if (nombreRestaurante.isNotEmpty()) {
            resenaViewModel.getResenasByRestauranteName(nombreRestaurante)
        } else {
            Toast.makeText(this, "Error: Nombre de restaurante no encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    // Corrección 1: Esta función ahora está fuera de onCreate()
    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter { resena ->
            // Navegar a los detalles de la reseña
            val intent = Intent(this, ReviewDetailsActivity::class.java)
            intent.putExtra("RESENA_ID", resena.id)
            intent.putExtra("RESTAURANT_NAME", nombreRestaurante) // Pasa el nombre para ReviewDetails
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RestaurantReviewsActivity)
            adapter = reviewAdapter
        }
    }

    private fun setupUI() {
        // Actualizar el título con el nombre del restaurante
        val tvToolbarTitle = findViewById<TextView>(R.id.tvToolbarTitle)
        tvToolbarTitle.text = if (nombreRestaurante.isNotEmpty()) {
            "Reseñas de $nombreRestaurante"
        } else {
            "Reseñas"
        }

        // Flecha de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Configuración del Bottom Navigation
        setupBottomNavigation()
    }

    private fun observeViewModel() {
        // Observar las reseñas
        resenaViewModel.resenas.observe(this) { resenas ->
            mostrarResenas(resenas)
        }

        // Observar estado de carga
        resenaViewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        resenaViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                resenaViewModel.clearError()
            }
        }
    }

    private fun mostrarResenas(resenas: List<Resena>) {
        if (resenas.isEmpty()) {
            // Mostrar mensaje de que no hay reseñas usando el layout predefinido
            recyclerView.visibility = View.GONE
            layoutNoReviews.visibility = View.VISIBLE
        } else {
            // Mostrar las reseñas en el RecyclerView
            layoutNoReviews.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            reviewAdapter.updateResenas(resenas)
        }
    }

    // Corrección 2: Eliminado el código suelto y la función redundante mostrarMensajeSinResenas()

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_populares -> {
                    // Si esta actividad no debería ser el destino final, considera finish()
                    // Si ya estás en populares o similar, no hagas nada
                    true
                }
                R.id.nav_postear -> {
                    startActivity(Intent(this, NuevaResenaActivity::class.java))
                    true
                }
                R.id.nav_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java))
                    true
                }
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}