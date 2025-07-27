package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.FavoritosRepository

class RestaurantDetailActivity : AppCompatActivity() {

    private var nombreRestaurante: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        nombreRestaurante = intent.getStringExtra("RESTAURANT_NAME") ?: ""
        if (nombreRestaurante.isEmpty()) {
            val tvTitle = findViewById<TextView>(R.id.tvTitle)
            nombreRestaurante = tvTitle.text.toString()
        }

        val closeDetail = findViewById<ImageView>(R.id.closeDetail)
        closeDetail.setOnClickListener { finish() }

        val btnViewReviews = findViewById<MaterialButton>(R.id.btnViewReviews)
        btnViewReviews.setOnClickListener {
            val intent = Intent(this, RestaurantReviewsActivity::class.java)
            intent.putExtra("RESTAURANT_NAME", nombreRestaurante)
            startActivity(intent)
        }

        val btnFavorite = findViewById<MaterialButton>(R.id.btnFavorite)
        var esFavorito = false

        btnFavorite.setOnClickListener {
            esFavorito = !esFavorito
            if (esFavorito) {
                btnFavorite.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange_buttons_filledStars)
                btnFavorite.setIconResource(R.drawable.favoritol)
                btnFavorite.text = "En Favoritos"

                val userId = obtenerUserIdActual()
                val repo = FavoritosRepository(this)
                val resenaId = repo.obtenerResenaIdPorNombre(nombreRestaurante)
                if (resenaId != null) {
                    repo.agregarFavorito(userId, resenaId)
                    Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se encontró la reseña", Toast.LENGTH_SHORT).show()
                }
            } else {
                btnFavorite.backgroundTintList = ContextCompat.getColorStateList(this, R.color.fill_buttons)
                btnFavorite.setIconResource(R.drawable.favoritol)
                btnFavorite.text = "Agregar a Favoritos"
                // Aquí podrías implementar eliminar de favoritos si lo deseas
            }
        }

        setupBottomNavigation()
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
                R.id.nav_populares -> true
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
        return 1L // Cambia esto por tu lógica real
    }
}