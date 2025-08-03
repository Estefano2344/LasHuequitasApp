package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.FavoritosRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import com.proano.estefano.lashuequitasapp.model.entities.User
import java.io.File
import java.util.Locale

class RestaurantDetailActivity : AppCompatActivity() {

    // Propiedades de la clase
    private var currentRestaurant: Restaurant? = null
    private lateinit var resenaRepository: ResenaRepository
    private lateinit var sessionManager: SessionManager

    // Componentes de la UI
    private lateinit var recyclerViewRecommended: RecyclerView
    private lateinit var noRecommendationsMessage: TextView
    private lateinit var recommendedRestaurantsAdapter: RecommendedRestaurantsAdapter
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

        // Ajustar paddings para la interfaz edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar lógica de negocio y sesión
        resenaRepository = ResenaRepository(this)
        sessionManager = SessionManager(this)

        // Obtener datos del restaurante pasados en el Intent
        currentRestaurant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("restaurant_data", Restaurant::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("restaurant_data") as? Restaurant
        }

        // Inicializar todas las vistas y configurar los listeners
        initializeViews()
        setupRecommendedRecyclerView()
        setupListeners()
        setupBottomNavigation()

        // Si el restaurante existe, poblar la UI. Si no, cerrar la actividad.
        currentRestaurant?.let { restaurant ->
            populateRestaurantDetails(restaurant)
            loadReviewStatistics(restaurant.name)
            loadRecommendedRestaurants()
        } ?: run {
            Toast.makeText(this, "Error al cargar datos del restaurante.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        recyclerViewRecommended = findViewById(R.id.recyclerViewRecommended)
        noRecommendationsMessage = findViewById(R.id.noRecommendationsMessage)
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
    }

    private fun populateRestaurantDetails(restaurant: Restaurant) {
        findViewById<TextView>(R.id.detailTitle).text = restaurant.name
        findViewById<TextView>(R.id.tvTitle).text = restaurant.name

        val ivRestaurant: ImageView = findViewById(R.id.ivRestaurant)
        val latestImagePath = resenaRepository.getLatestRestaurantImage(restaurant.name)

        if (!latestImagePath.isNullOrEmpty()) {
            val imageFile = File(latestImagePath)
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).placeholder(R.drawable.placeholder_restaurant).error(R.drawable.placeholder_restaurant).into(ivRestaurant)
            } else {
                loadDefaultImage(restaurant, ivRestaurant)
            }
        } else {
            loadDefaultImage(restaurant, ivRestaurant)
        }

        findViewById<TextView>(R.id.tvRatingValue).text = String.format(Locale.getDefault(), "%.1f", restaurant.rating)
        findViewById<androidx.appcompat.widget.AppCompatRatingBar>(R.id.ratingBarDetail).rating = restaurant.rating
        findViewById<TextView>(R.id.tvReviewsCount).text = getString(R.string.resenas_format, restaurant.reviewCount)
    }

    private fun loadDefaultImage(restaurant: Restaurant, imageView: ImageView) {
        if (restaurant.imageUrl.startsWith("content://") || restaurant.imageUrl.startsWith("file://")) {
            Glide.with(this).load(Uri.parse(restaurant.imageUrl)).placeholder(R.drawable.placeholder_restaurant).error(R.drawable.placeholder_restaurant).into(imageView)
        } else {
            val imageResId = resources.getIdentifier(restaurant.imageUrl, "drawable", packageName)
            if (imageResId != 0) {
                Glide.with(this).load(imageResId).placeholder(R.drawable.placeholder_restaurant).error(R.drawable.placeholder_restaurant).into(imageView)
            } else {
                imageView.setImageResource(R.drawable.placeholder_restaurant)
            }
        }
    }

    private fun loadReviewStatistics(restaurantName: String) {
        val reviews = resenaRepository.buscarResenasPorNombreRestaurante(restaurantName)
        val totalReviews = reviews.size

        if (totalReviews == 0) {

            val starViews = listOf(5 to (progressBar5Star to tvPercentage5Star), 4 to (progressBar4Star to tvPercentage4Star), 3 to (progressBar3Star to tvPercentage3Star), 2 to (progressBar2Star to tvPercentage2Star), 1 to (progressBar1Star to tvPercentage1Star))
            starViews.forEach { (_, views) ->
                views.first.progress = 0
                views.second.text = "0%"
            }
            return
        }

        val starCounts = mutableMapOf<Int, Int>()
        (1..5).forEach { starCounts[it] = 0 }

        reviews.forEach { resena ->
            val rating = resena.calificacion.toInt()
            if (starCounts.containsKey(rating)) {
                starCounts[rating] = starCounts.getOrDefault(rating, 0) + 1
            }
        }

        fun updateStarView(progressBar: ProgressBar, percentageTextView: TextView, count: Int) {
            val percentage = (count.toFloat() / totalReviews * 100).toInt()
            progressBar.progress = percentage
            percentageTextView.text = "$percentage%"
        }

        updateStarView(progressBar5Star, tvPercentage5Star, starCounts[5]!!)
        updateStarView(progressBar4Star, tvPercentage4Star, starCounts[4]!!)
        updateStarView(progressBar3Star, tvPercentage3Star, starCounts[3]!!)
        updateStarView(progressBar2Star, tvPercentage2Star, starCounts[2]!!)
        updateStarView(progressBar1Star, tvPercentage1Star, starCounts[1]!!)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.closeDetail).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnViewReviews).setOnClickListener {
            currentRestaurant?.let { restaurant ->
                val intent = Intent(this, RestaurantReviewsActivity::class.java)
                intent.putExtra("RESTAURANT_NAME", restaurant.name)
                startActivity(intent)
            }
        }

        val btnFavorite = findViewById<MaterialButton>(R.id.btnFavorite)
        val userId = obtenerUserIdActual()
        val repo = FavoritosRepository(this)
        val restaurantId = currentRestaurant?.id ?: -1L

        // Consultar si ya es favorito
        var esFavorito = repo.estaEnFavoritos(userId, restaurantId)
        actualizarBotonFavorito(esFavorito, btnFavorite)

        btnFavorite.setOnClickListener {
            if (esFavorito) {
                repo.eliminarDeFavoritos(userId, restaurantId)
                esFavorito = false
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                repo.agregarFavorito(userId, restaurantId)
                esFavorito = true
                Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
            }
            actualizarBotonFavorito(esFavorito, btnFavorite)
        }
    }

    private fun actualizarBotonFavorito(esFavorito: Boolean, btn: MaterialButton) {
        if (esFavorito) {
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange_buttons_filledStars)
            btn.setIconResource(R.drawable.corazonb)
            btn.text = "En Favoritos"
        } else {
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.fill_buttons)
            btn.setIconResource(R.drawable.corazonw) // Cambia si tienes un icono diferente para "no favorito"
            btn.text = "Agregar a Favoritos"
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_populares -> {
                    startActivity(Intent(this, PopularesActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
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



    private fun obtenerUserIdActual(): Long {
        return sessionManager.getUserId()
    }

    private fun setupRecommendedRecyclerView() {
        // Asumiendo que RecommendedRestaurantsAdapter existe y está correctamente implementado
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

        val userPreferences = currentUser.preferenciasGastronomicas?.split(",")?.map { it.trim() } ?: emptyList()

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