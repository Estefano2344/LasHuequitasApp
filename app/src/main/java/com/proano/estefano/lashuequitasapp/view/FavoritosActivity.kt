package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.FavoritosRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.Favorito
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant

class FavoritosActivity : AppCompatActivity() {
    private lateinit var listaRestaurantes: List<Restaurant>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_favoritos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                    val intent = Intent(this, NuevaResenaActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_favoritos -> true
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        val closeProfile = findViewById<ImageView>(R.id.closeProfile)
        closeProfile.setOnClickListener {
            finish()
        }

        cargarFavoritos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritos()
    }

    private fun cargarFavoritos() {
        val userId = obtenerUserIdActual()
        val favoritosRepository = FavoritosRepository(this)
        val listaFavoritos = favoritosRepository.obtenerFavoritos(userId)

        val resenaRepository = ResenaRepository(this)
        listaRestaurantes = resenaRepository.getRecommendedRestaurants() + resenaRepository.getPopularRestaurants()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerFavoritos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FavoritosAdapter(listaFavoritos) { favorito ->
            val restaurant = listaRestaurantes.find { it.name == favorito.nombre }
            if (restaurant != null) {
                val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("restaurant_data", restaurant)
                startActivity(intent)
            }
        }
    }

    private fun obtenerUserIdActual(): Long {
        return 1L // Cambia esto por tu lógica real de usuario
    }
}