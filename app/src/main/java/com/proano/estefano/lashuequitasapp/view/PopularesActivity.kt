package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import java.io.File

class PopularesActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var resenaRepository: ResenaRepository
    private lateinit var popularRestaurantsContainer: LinearLayout
    private lateinit var noRestaurantsMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_populares)

        // Ajuste de inset para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar repositorios y session manager
        sessionManager = SessionManager(this)
        resenaRepository = ResenaRepository(this)

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // Inicializar vistas
        popularRestaurantsContainer = findViewById(R.id.popularRestaurantsContainer)
        noRestaurantsMessage = findViewById(R.id.noRestaurantsMessage)

        setupListeners()
        setupBottomNavigation()
        loadPopularRestaurants()
    }

    override fun onResume() {
        super.onResume()
        loadPopularRestaurants()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupListeners() {
        // Cerrar Activity al pulsar la flecha de atrás
        findViewById<ImageView>(R.id.closeProfile).setOnClickListener {
            finish()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.selectedItemId = R.id.nav_populares // Marcar como seleccionado

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
                    // Ya estamos en la pantalla de populares
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

    private fun loadPopularRestaurants() {
        val popularList = resenaRepository.getPopularRestaurants()

        if (popularList.isEmpty()) {
            popularRestaurantsContainer.visibility = View.GONE
            noRestaurantsMessage.visibility = View.VISIBLE
            noRestaurantsMessage.text = getString(R.string.no_restaurants_available)
        } else {
            popularRestaurantsContainer.visibility = View.VISIBLE
            noRestaurantsMessage.visibility = View.GONE
            displayPopularRestaurants(popularList)
        }
    }

    private fun displayPopularRestaurants(restaurants: List<Restaurant>) {
        popularRestaurantsContainer.removeAllViews()

        val inflater = LayoutInflater.from(this)

        restaurants.forEach { restaurant ->
            val restaurantView = inflater.inflate(R.layout.item_popular_restaurant_full, popularRestaurantsContainer, false)

            val imageView = restaurantView.findViewById<ImageView>(R.id.restaurantImageView)
            val nameTextView = restaurantView.findViewById<TextView>(R.id.restaurantNameTextView)
            val priceRangeTextView = restaurantView.findViewById<TextView>(R.id.restaurantPriceRangeTextView)
            val ratingTextView = restaurantView.findViewById<TextView>(R.id.restaurantRatingTextView)
            val reviewCountTextView = restaurantView.findViewById<TextView>(R.id.restaurantReviewCountTextView)
            val foodTypeTextView = restaurantView.findViewById<TextView>(R.id.restaurantFoodTypeTextView)
            val viewRestaurantButton = restaurantView.findViewById<MaterialButton>(R.id.btnVerRestaurante)

            nameTextView.text = restaurant.name
            ratingTextView.text = getString(R.string.puntuacion_format, restaurant.rating)
            reviewCountTextView.text = getString(R.string.resenas_format, restaurant.reviewCount)
            foodTypeTextView.text = restaurant.foodType

            // Obtener información adicional del restaurante desde las reseñas
            val restaurantReviews = resenaRepository.buscarResenasPorNombreRestaurante(restaurant.name)
            val priceRange = if (restaurantReviews.isNotEmpty()) {
                restaurantReviews.first().rangoPrecio
            } else {
                getString(R.string.precio_no_disponible)
            }
            priceRangeTextView.text = priceRange

            // Obtener la imagen más reciente del restaurante desde las reseñas
            val latestImagePath = resenaRepository.getLatestRestaurantImage(restaurant.name)

            if (!latestImagePath.isNullOrEmpty()) {
                val imageFile = File(latestImagePath)
                if (imageFile.exists()) {
                    Glide.with(this)
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_restaurant)
                        .error(R.drawable.placeholder_restaurant)
                        .into(imageView)
                } else {
                    loadDefaultRestaurantImage(restaurant, imageView)
                }
            } else {
                loadDefaultRestaurantImage(restaurant, imageView)
            }

            // Configurar el botón "Ver Restaurante"
            viewRestaurantButton.setOnClickListener {
                val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_data", restaurant)
                startActivity(intent)
            }

            popularRestaurantsContainer.addView(restaurantView)
        }
    }

    private fun loadDefaultRestaurantImage(restaurant: Restaurant, imageView: ImageView) {
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
}