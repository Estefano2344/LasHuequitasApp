package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class RestaurantDetailActivity : AppCompatActivity() {
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

        // 1) Cerrar al pulsar la flecha
        val closeDetail = findViewById<ImageView>(R.id.closeDetail)
        closeDetail.setOnClickListener {
            finish()
        }

        // 2) Ir a RestaurantReviewsActivity
        val btnViewReviews = findViewById<MaterialButton>(R.id.btnViewReviews)
        btnViewReviews.setOnClickListener {
            val intent = Intent(this, RestaurantReviewsActivity::class.java)
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
                    finish()  // Cierra la actividad actual (PopularesActivity)
                    true
                }
                // Navegar a la pantalla de populares (PopularesActivity)
                R.id.nav_populares -> {
                    // Ya estamos en la pantalla de populares, no hacer nada
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
}
