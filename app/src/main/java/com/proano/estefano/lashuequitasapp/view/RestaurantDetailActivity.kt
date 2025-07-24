package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R

class RestaurantDetailActivity : AppCompatActivity() {

    // Variable para almacenar el nombre del restaurante
    private var nombreRestaurante: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_detail)

        // Ajuste de inset para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener el nombre del restaurante del Intent o del TextView
        nombreRestaurante = intent.getStringExtra("RESTAURANT_NAME") ?: ""

        // Si no viene del Intent, obtenerlo del TextView (para casos de datos estáticos)
        if (nombreRestaurante.isEmpty()) {
            val tvTitle = findViewById<TextView>(R.id.tvTitle)
            nombreRestaurante = tvTitle.text.toString()
        }

        // 1) Cerrar al pulsar la flecha
        val closeDetail = findViewById<ImageView>(R.id.closeDetail)
        closeDetail.setOnClickListener {
            finish()
        }

        // 2) Ir a RestaurantReviewsActivity pasando el nombre del restaurante
        val btnViewReviews = findViewById<MaterialButton>(R.id.btnViewReviews)
        btnViewReviews.setOnClickListener {
            val intent = Intent(this, RestaurantReviewsActivity::class.java)
            // IMPORTANTE: Pasar el nombre del restaurante
            intent.putExtra("RESTAURANT_NAME", nombreRestaurante)
            startActivity(intent)
        }

        // Configuración del Bottom Navigation
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
                R.id.nav_populares -> {
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