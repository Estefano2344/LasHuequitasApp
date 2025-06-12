package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val opciones = findViewById<ImageView>(R.id.opcionesImageView)
        opciones.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Navegar a la actividad de búsqueda
        findViewById<TextInputEditText>(R.id.searchEditText).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        // Configurar BottomNavigationView
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                // Navegar a la pantalla del perfil
                R.id.nav_perfil -> {
                    startActivity(Intent(this, PerfilActivity::class.java))
                    true
                }
                // Navegar a la pantalla de populares
                R.id.nav_populares -> {
                    startActivity(Intent(this, PopularesActivity::class.java))
                    true
                }
                // Navegar a la pantalla para postear una reseña
                R.id.nav_postear -> {
                    startActivity(Intent(this, NuevaResenaActivity::class.java))
                    true
                }
                // Navegar a la pantalla de favoritos
                R.id.nav_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
