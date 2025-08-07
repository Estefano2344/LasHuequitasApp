// src/main/java/com/proano/estefano/lashuequitasapp/view/HomeActivity.kt
package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import java.io.File

class HomeActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var resenaRepository: ResenaRepository
    private lateinit var recommendedRestaurantsContainer: LinearLayout
    private lateinit var popularRestaurantsContainer: LinearLayout
    private lateinit var recommendedTitle: TextView
    private lateinit var popularTitle: TextView
    private lateinit var recommendedScroll: HorizontalScrollView
    private lateinit var popularScroll: HorizontalScrollView
    private lateinit var noRestaurantsMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        resenaRepository = ResenaRepository(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        recommendedRestaurantsContainer = findViewById(R.id.recommendedRestaurantsContainer)
        popularRestaurantsContainer = findViewById(R.id.popularRestaurantsContainer)
        recommendedTitle = findViewById(R.id.recomendadosTitle)
        popularTitle = findViewById(R.id.popularesTitle)
        recommendedScroll = findViewById(R.id.recomendadosScroll)
        popularScroll = findViewById(R.id.popularesScroll)
        noRestaurantsMessage = findViewById(R.id.noRestaurantsMessage)

        setupUI()
        setupNavigationDrawer()
    }

    override fun onResume() {
        super.onResume()
        loadRestaurants()
    }

    private fun setupUI() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val opciones = findViewById<ImageView>(R.id.opcionesImageView)
        opciones.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<TextInputEditText>(R.id.searchEditText).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { true } // Already on Home
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
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
                else -> false
            }
        }
    }

    private fun setupNavigationDrawer() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        NavDrawerHandler(this, headerView, sessionManager)
    }

    private fun navigateToLogin() {
        val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadRestaurants() {
        val recommendedList = resenaRepository.getRecommendedRestaurants()
        val popularList = resenaRepository.getPopularRestaurants()

        if (recommendedList.isEmpty() && popularList.isEmpty()) {
            recommendedTitle.visibility = View.GONE
            popularTitle.visibility = View.GONE
            recommendedScroll.visibility = View.GONE
            popularScroll.visibility = View.GONE
            noRestaurantsMessage.visibility = View.VISIBLE
        } else {
            recommendedTitle.visibility = View.VISIBLE
            popularTitle.visibility = View.VISIBLE
            recommendedScroll.visibility = View.VISIBLE
            popularScroll.visibility = View.VISIBLE
            noRestaurantsMessage.visibility = View.GONE

            displayRestaurants(recommendedList, recommendedRestaurantsContainer) { restaurant ->
                val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_data", restaurant)
                startActivity(intent)
            }
            displayRestaurants(popularList, popularRestaurantsContainer) { restaurant ->
                val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_data", restaurant)
                startActivity(intent)
            }
        }
    }

    private fun displayRestaurants(restaurants: List<Restaurant>, container: LinearLayout, clickListener: (Restaurant) -> Unit) {
        container.removeAllViews()

        val inflater = LayoutInflater.from(this)

        restaurants.forEach { restaurant ->
            val restaurantView = inflater.inflate(R.layout.item_restaurant_card, container, false)

            val imageView = restaurantView.findViewById<ImageView>(R.id.restaurantImageView)
            val nameTextView = restaurantView.findViewById<TextView>(R.id.restaurantNameTextView)
            val ratingTextView = restaurantView.findViewById<TextView>(R.id.restaurantRatingTextView)
            val reviewCountTextView = restaurantView.findViewById<TextView>(R.id.restaurantReviewCountTextView)

            nameTextView.text = restaurant.name
            ratingTextView.text = getString(R.string.puntuacion_format, restaurant.rating)
            reviewCountTextView.text = getString(R.string.resenas_format, restaurant.reviewCount)

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
                        .into(imageView)
                } else {
                    loadDefaultRestaurantImage(restaurant, imageView)
                }
            } else {
                // Si no hay imagen de reseña, usar la lógica original
                loadDefaultRestaurantImage(restaurant, imageView)
            }

            // Set the click listener for the item view
            restaurantView.setOnClickListener {
                clickListener(restaurant)
            }

            container.addView(restaurantView)
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