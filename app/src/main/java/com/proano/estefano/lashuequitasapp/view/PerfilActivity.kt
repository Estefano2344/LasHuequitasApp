package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository
import java.io.File

class PerfilActivity : AppCompatActivity() {

    private lateinit var userRepository: UserRepository
    private lateinit var resenaRepository: ResenaRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userRepository = UserRepository(this)
        resenaRepository = ResenaRepository(this)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = prefs.getLong("user_id", -1)

        if (userId != -1L) {
            val currentUser = userRepository.getUserById(userId)
            if (currentUser != null) {
                findViewById<TextView>(R.id.profileName).text =
                    "${currentUser.nombre} ${currentUser.apellido}"
                findViewById<TextView>(R.id.profileUsername).text = currentUser.usuario
                findViewById<TextView>(R.id.profileEmail).text = currentUser.email

                currentUser.foto?.let {
                    val file = File(it)
                    if (file.exists()) {
                        Glide.with(this)
                            .load(file)
                            .placeholder(R.drawable.perfilexam)
                            .circleCrop()
                            .into(findViewById(R.id.profileImage))
                    }
                }

                val prefsContainer = findViewById<LinearLayout>(R.id.preferenciasContainer)
                prefsContainer.removeAllViews()
                if (!currentUser.preferenciasGastronomicas.isNullOrEmpty()) {
                    currentUser.preferenciasGastronomicas.split(",").forEach { pref ->
                        val chip = TextView(this).apply {
                            text = pref.trim()
                            setPadding(24, 8, 24, 8)
                            setBackgroundResource(R.color.background_fields)
                            setTextColor(resources.getColor(R.color.black))

                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            params.setMargins(8, 0, 8, 0)
                            layoutParams = params
                        }
                        prefsContainer.addView(chip)
                    }
                } else {
                    val noPrefs = TextView(this).apply {
                        text = "Sin preferencias registradas"
                        setTextColor(resources.getColor(R.color.black))
                    }
                    prefsContainer.addView(noPrefs)
                }

                val resenasContainer = findViewById<LinearLayout>(R.id.resenasRecientesContainer)
                resenasContainer.removeAllViews()
                val resenas = resenaRepository.getResenasByUserId(currentUser.id)
                if (resenas.isNotEmpty()) {
                    val inflater = LayoutInflater.from(this)
                    resenas.take(3).forEach { resena ->
                        val itemView = inflater.inflate(R.layout.item_resena_perfil, resenasContainer, false)
                        val imageView = itemView.findViewById<ImageView>(R.id.resenaImage)
                        val titleView = itemView.findViewById<TextView>(R.id.resenaTitle)
                        val previewView = itemView.findViewById<TextView>(R.id.resenaPreview)

                        val primeraImagen = resena.imagenes.split(",").firstOrNull()
                        if (!primeraImagen.isNullOrEmpty()) {
                            val imgFile = File(primeraImagen)
                            if (imgFile.exists()) {
                                Glide.with(this).load(imgFile).into(imageView)
                            }
                        }

                        titleView.text = resena.nombreRestaurante
                        previewView.text = if (resena.comentarios.length > 50)
                            "${resena.comentarios.take(50)}..."
                        else resena.comentarios

                        resenasContainer.addView(itemView)
                    }
                } else {
                    val noResenas = TextView(this).apply {
                        text = "Aún no tienes reseñas"
                        setTextColor(resources.getColor(R.color.black))
                    }
                    resenasContainer.addView(noResenas)
                }
            } else {
                Toast.makeText(this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show()
            }
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java)
                        .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK })
                    finish()
                    true
                }
                R.id.nav_populares -> {
                    startActivity(Intent(this, PopularesActivity::class.java)
                        .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK })
                    finish()
                    true
                }
                R.id.nav_postear -> {
                    startActivity(Intent(this, NuevaResenaActivity::class.java)
                        .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK })
                    finish()
                    true
                }
                R.id.nav_favoritos -> {
                    startActivity(Intent(this, FavoritosActivity::class.java)
                        .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK })
                    finish()
                    true
                }
                R.id.nav_perfil -> true
                else -> false
            }
        }

        findViewById<ImageView>(R.id.closeProfile).setOnClickListener { finish() }
        findViewById<Button>(R.id.editProfileButton).setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
    }
}
