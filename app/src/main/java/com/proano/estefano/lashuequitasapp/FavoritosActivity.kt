package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class FavoritosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Habilitar los bordes de pantalla completa
        setContentView(R.layout.activity_favoritos) // Asegúrate de que el layout correcto esté asociado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()) // Ajuste para los márgenes del sistema
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom) // Aplicar los márgenes del sistema
            insets
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
                    finish()  // Cierra la actividad actual (FavoritosActivity)
                    true
                }
                // Navegar a la pantalla de populares (PopularesActivity)
                R.id.nav_populares -> {
                    val intent = Intent(this, PopularesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (FavoritosActivity)
                    true
                }
                // Navegar a la pantalla para postear una reseña (NuevaResenaActivity)
                R.id.nav_postear -> {
                    val intent = Intent(this, NuevaResenaActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (FavoritosActivity)
                    true
                }
                // Ya estamos en la pantalla de favoritos, no hacer nada
                R.id.nav_favoritos -> {
                    true  // No hace nada porque ya estamos en esta pantalla
                }
                // Navegar a la pantalla del perfil (PerfilActivity)
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (FavoritosActivity)
                    true
                }
                else -> false
            }
        }



        // Botón de cerrar (que probablemente cierra esta actividad)
        val closeProfile = findViewById<ImageView>(R.id.closeProfile)
        closeProfile.setOnClickListener {
            finish() // Finaliza la actividad actual (cerrar pantalla de favoritos)
        }
    }
}
