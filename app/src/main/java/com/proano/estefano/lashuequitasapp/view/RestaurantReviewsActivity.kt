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
import com.proano.estefano.lashuequitasapp.view.ReviewAdapter
import com.proano.estefano.lashuequitasapp.viewmodel.ResenaViewModel

class RestaurantReviewsActivity : AppCompatActivity() {

    private lateinit var resenaViewModel: ResenaViewModel
    private lateinit var reviewsAdapter: ReviewAdapter
    private lateinit var recyclerViewReviews: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutNoReviews: LinearLayout
    private lateinit var tvToolbarTitle: TextView

    private var restaurantName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_reviews)

        // Ajuste de inset para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Obtener el nombre del restaurante desde el Intent
        restaurantName = intent.getStringExtra("restaurant_name") ?: ""
        tvToolbarTitle.text = "Reseñas - $restaurantName"

        // Cargar las reseñas
        loadReviews()
    }

    private fun initViews() {
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews)
        progressBar = findViewById(R.id.progressBar)
        layoutNoReviews = findViewById(R.id.layoutNoReviews)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
    }

    private fun setupViewModel() {
        resenaViewModel = ViewModelProvider(this)[ResenaViewModel::class.java]
    }

    private fun setupRecyclerView() {
        reviewsAdapter = ReviewAdapter { resena ->
            // Callback cuando se presiona "Ver reseña completa"
            val intent = Intent(this, ReviewDetailsActivity::class.java)
            intent.putExtra("resena_id", resena.id)
            startActivity(intent)
        }

        recyclerViewReviews.apply {
            layoutManager = LinearLayoutManager(this@RestaurantReviewsActivity)
            adapter = reviewsAdapter
        }
    }

    private fun setupObservers() {
        // Observar las reseñas
        resenaViewModel.resenas.observe(this) { resenas ->
            if (resenas.isNotEmpty()) {
                reviewsAdapter.updateResenas(resenas)
                recyclerViewReviews.visibility = View.VISIBLE
                layoutNoReviews.visibility = View.GONE
            } else {
                recyclerViewReviews.visibility = View.GONE
                layoutNoReviews.visibility = View.VISIBLE
            }
        }

        // Observar el estado de carga
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

    private fun setupListeners() {
        // Botón de retroceso
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Configuración del Bottom Navigation
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
                    startActivity(Intent(this, PopularesActivity::class.java))
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

    private fun loadReviews() {
        if (restaurantName.isNotEmpty()) {
            resenaViewModel.getResenasByRestauranteName(restaurantName)
        } else {
            // Si no hay nombre específico, cargar todas las reseñas
            resenaViewModel.getAllResenas()
        }
    }
}