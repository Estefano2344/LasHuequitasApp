package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class PopularesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_populares)

        // Ajuste de inset para edge-to-edge sobre el root correcto
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cerrar Activity al pulsar la flecha de atrás
        findViewById<ImageView>(R.id.closeProfile).setOnClickListener {
            finish()
        }

        // Configurar el botón para "Ver Restaurante"
        val btnVerRestaurante1 = findViewById<Button>(R.id.btnVerRestaurante1)
        btnVerRestaurante1.setOnClickListener {
            val intent = Intent(this, RestaurantDetailActivity::class.java)
            startActivity(intent) // Navegar a RestaurantDetailActivity
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
                    val intent = Intent(this, NuevaResenaActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (PopularesActivity)
                    true
                }
                // Navegar a la pantalla de favoritos (FavoritosActivity)
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (PopularesActivity)
                    true
                }
                // Navegar a la pantalla del perfil (PerfilActivity)
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (PopularesActivity)
                    true
                }
                else -> false
            }
        }
    }
}
