package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.model.entities.User
import java.io.File
import java.util.Locale

class RestaurantDetailActivity : AppCompatActivity() {

    private var currentRestaurant: Restaurant? = null
    private lateinit var resenaRepository: ResenaRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var recyclerViewRecommended: RecyclerView
    private lateinit var noRecommendationsMessage: TextView
    private lateinit var recommendedRestaurantsAdapter: RecommendedRestaurantsAdapter

    // Vistas para las barras de progreso y porcentajes
    private lateinit var progressBar5Star: ProgressBar
    private lateinit var tvPercentage5Star: TextView
    private lateinit var progressBar4Star: ProgressBar
    private lateinit var tvPercentage4Star: TextView
    private lateinit var progressBar3Star: ProgressBar
    private lateinit var tvPercentage3Star: TextView
    private lateinit var progressBar2Star: ProgressBar
    private lateinit var tvPercentage2Star: TextView
    private lateinit var progressBar1Star: ProgressBar
    private lateinit var tvPercentage1Star: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resenaRepository = ResenaRepository(this)
        sessionManager = SessionManager(this)

        currentRestaurant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("restaurant_data", Restaurant::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("restaurant_data") as? Restaurant
        }

        recyclerViewRecommended = findViewById(R.id.recyclerViewRecommended)
        noRecommendationsMessage = findViewById(R.id.noRecommendationsMessage)

        // Inicializar vistas de progreso y porcentajes
        progressBar5Star = findViewById(R.id.progressBar5Star)
        tvPercentage5Star = findViewById(R.id.tvPercentage5Star)
        progressBar4Star = findViewById(R.id.progressBar4Star)
        tvPercentage4Star = findViewById(R.id.tvPercentage4Star)
        progressBar3Star = findViewById(R.id.progressBar3Star)
        tvPercentage3Star = findViewById(R.id.tvPercentage3Star)
        progressBar2Star = findViewById(R.id.progressBar2Star)
        tvPercentage2Star = findViewById(R.id.tvPercentage2Star)
        progressBar1Star = findViewById(R.id.progressBar1Star)
        tvPercentage1Star = findViewById(R.id.tvPercentage1Star)


        setupRecommendedRecyclerView()

        currentRestaurant?.let { restaurant ->
            populateRestaurantDetails(restaurant)
            loadReviewStatistics(restaurant.name)
            loadRecommendedRestaurants()
        } ?: run {
            finish()
        }

        setupListeners()
        setupBottomNavigation()
    }

    private fun populateRestaurantDetails(restaurant: Restaurant) {
        val detailTitle: TextView = findViewById(R.id.detailTitle)
        detailTitle.text = restaurant.name

        val tvTitle: TextView = findViewById(R.id.tvTitle)
        tvTitle.text = restaurant.name

        val ivRestaurant: ImageView = findViewById(R.id.ivRestaurant)

        // Obtener la imagen más reciente del restaurante desde las reseñas
        val latestImagePath = resenaRepository.getLatestRestaurantImage(restaurant.name)

        if (!latestImagePath.isNullOrEmpty()) {
            // Si hay una imagen de reseña, usarla
            val imageFile = File(latestImagePath)
            if (imageFile.exists()) {
                Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(ivRestaurant)
            } else {
                loadDefaultImage(restaurant, ivRestaurant)
            }
        } else {
            // Si no hay imagen de reseña, usar la lógica original
            loadDefaultImage(restaurant, ivRestaurant)
        }

        val tvRatingValue: TextView = findViewById(R.id.tvRatingValue)
        tvRatingValue.text = String.format(Locale.getDefault(), "%.1f", restaurant.rating)

        val ratingBarDetail: androidx.appcompat.widget.AppCompatRatingBar = findViewById(R.id.ratingBarDetail)
        ratingBarDetail.rating = restaurant.rating

        val tvReviewsCount: TextView = findViewById(R.id.tvReviewsCount)
        tvReviewsCount.text = getString(R.string.resenas_format, restaurant.reviewCount)
    }

    private fun loadDefaultImage(restaurant: Restaurant, imageView: ImageView) {
        if (restaurant.imageUrl.startsWith("content://") || restaurant.imageUrl.startsWith("file://")) {
            Glide.with(this)
                .load(Uri.parse(restaurant.imageUrl))
                .placeholder(R.drawable.placeholder_restaurant)
                .error(R.drawable.placeholder_restaurant)
                .into(imageView)
        } else {
            val imageResId = resources.getIdentifier(restaurant.imageUrl, "drawable", packageName)
            if (imageResId != 0) {
                Glide.with(this)
                    .load(imageResId)
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.placeholder_restaurant)
            }
        }
    }

    private fun loadReviewStatistics(restaurantName: String) {
        val reviews = resenaRepository.buscarResenasPorNombreRestaurante(restaurantName)
        val totalReviews = reviews.size

        if (totalReviews == 0) {
            // Si no hay reseñas, establecer todo a 0
            progressBar5Star.progress = 0
            tvPercentage5Star.text = "0%"
            progressBar4Star.progress = 0
            tvPercentage4Star.text = "0%"
            progressBar3Star.progress = 0
            tvPercentage3Star.text = "0%"
            progressBar2Star.progress = 0
            tvPercentage2Star.text = "0%"
            progressBar1Star.progress = 0
            tvPercentage1Star.text = "0%"
            return
        }

        val starCounts = mutableMapOf<Int, Int>()
        for (i in 1..5) starCounts[i] = 0 // Inicializar conteos

        reviews.forEach { resena ->
            val rating = resena.calificacion.toInt() // Asumiendo calificaciones enteras para el conteo
            if (starCounts.containsKey(rating)) {
                starCounts[rating] = starCounts[rating]!! + 1
            }
        }

        // Calcular porcentajes y actualizar vistas
        fun updateStarView(star: Int, progressBar: ProgressBar, percentageTextView: TextView) {
            val count = starCounts[star] ?: 0
            val percentage = if (totalReviews > 0) ((count.toFloat() / totalReviews) * 100).toInt() else 0
            progressBar.progress = percentage
            percentageTextView.text = "$percentage%"
        }

        updateStarView(5, progressBar5Star, tvPercentage5Star)
        updateStarView(4, progressBar4Star, tvPercentage4Star)
        updateStarView(3, progressBar3Star, tvPercentage3Star)
        updateStarView(2, progressBar2Star, tvPercentage2Star)
        updateStarView(1, progressBar1Star, tvPercentage1Star)
    }


    private fun setupListeners() {
        val closeDetail = findViewById<ImageView>(R.id.closeDetail)
        closeDetail.setOnClickListener {
            finish()
        }

        val btnViewReviews = findViewById<MaterialButton>(R.id.btnViewReviews)
        btnViewReviews.setOnClickListener {
            currentRestaurant?.let {
                val intent = Intent(this, RestaurantReviewsActivity::class.java)
                intent.putExtra("RESTAURANT_NAME", it.name)
                startActivity(intent)
            }
        }
    }

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
                    val intent = Intent(this, PopularesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
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

    private fun setupRecommendedRecyclerView() {
        recommendedRestaurantsAdapter = RecommendedRestaurantsAdapter(emptyList(), resenaRepository)
        recyclerViewRecommended.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewRecommended.adapter = recommendedRestaurantsAdapter
        recyclerViewRecommended.isNestedScrollingEnabled = false
    }

    private fun loadRecommendedRestaurants() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            findViewById<TextView>(R.id.tvRecommendedTitle).visibility = View.GONE
            recyclerViewRecommended.visibility = View.GONE
            noRecommendationsMessage.text = getString(R.string.login_for_recommendations)
            noRecommendationsMessage.visibility = View.VISIBLE
            return
        }

        val currentUser: User? = resenaRepository.getUserById(userId)

        if (currentUser == null) {
            findViewById<TextView>(R.id.tvRecommendedTitle).visibility = View.GONE
            recyclerViewRecommended.visibility = View.GONE
            noRecommendationsMessage.text = getString(R.string.login_for_recommendations)
            noRecommendationsMessage.visibility = View.VISIBLE
            return
        }

        val userPreferencesString = currentUser.preferenciasGastronomicas
        val userPreferences = if (userPreferencesString.isNullOrBlank()) {
            emptyList()
        } else {
            userPreferencesString.split(",").map { it.trim() }
        }

        if (userPreferences.isEmpty()) {
            findViewById<TextView>(R.id.tvRecommendedTitle).visibility = View.GONE
            recyclerViewRecommended.visibility = View.GONE
            noRecommendationsMessage.text = getString(R.string.no_preferences_set)
            noRecommendationsMessage.visibility = View.VISIBLE
            return
        }

        val allRecommended = resenaRepository.getRestaurantsByFoodTypes(userPreferences)
        val filteredRecommended = allRecommended.filter { it.name != currentRestaurant?.name }

        if (filteredRecommended.isEmpty()) {
            noRecommendationsMessage.visibility = View.VISIBLE
            recyclerViewRecommended.visibility = View.GONE
        } else {
            noRecommendationsMessage.visibility = View.GONE
            recyclerViewRecommended.visibility = View.VISIBLE
            recommendedRestaurantsAdapter.updateData(filteredRecommended)
        }
    }
}