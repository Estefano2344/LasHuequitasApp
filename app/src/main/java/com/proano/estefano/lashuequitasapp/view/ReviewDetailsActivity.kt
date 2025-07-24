package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import com.proano.estefano.lashuequitasapp.viewmodel.ResenaViewModel
import com.proano.estefano.lashuequitasapp.viewmodel.ComentarioViewModel
import java.io.File
import android.graphics.BitmapFactory
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.proano.estefano.lashuequitasapp.model.entities.Comentario
import java.text.SimpleDateFormat
import java.util.*

class ReviewDetailsActivity : AppCompatActivity() {

    private lateinit var resenaViewModel: ResenaViewModel
    private lateinit var comentarioViewModel: ComentarioViewModel
    private lateinit var tvReviewTitle: TextView
    private lateinit var tvReviewDescription: TextView
    private lateinit var ivReviewImage: ImageView
    private lateinit var tvRestaurantName: TextView
    private lateinit var tvReviewDateDetail: TextView
    private lateinit var tvTipoComidaDetail: TextView
    private lateinit var tvRangoPrecioDetail: TextView
    private lateinit var tvUbicacionDetail: TextView
    private lateinit var ratingBarDetail: androidx.appcompat.widget.AppCompatRatingBar
    private lateinit var progressBar: ProgressBar
    private lateinit var comentariosContainer: LinearLayout // Este debe ser un contenedor ya en tu layout XML
    private lateinit var tvCommentsTitle: TextView
    private lateinit var tvNoComments: TextView
    private lateinit var progressBarComments: ProgressBar

    private var resenaId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review_details)

        // Ajuste de inset para edge-to-edge sobre el root correcto
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.detail_root)
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupViewModels()
        setupObservers()
        setupListeners()

        // Obtener el ID de la reseña desde el Intent
        resenaId = intent.getLongExtra("resena_id", -1)

        if (resenaId != -1L) {
            loadReviewDetails()
            loadComentarios()
        } else {
            Toast.makeText(this, "Error: No se pudo cargar la reseña", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar comentarios al volver de la actividad de nuevo comentario
        if (resenaId != -1L) {
            loadComentarios()
        }
    }

    private fun initViews() {
        tvReviewTitle = findViewById(R.id.tvReviewTitle)
        tvReviewDescription = findViewById(R.id.tvReviewDescription)
        ivReviewImage = findViewById(R.id.ivReviewImage)
        tvRestaurantName = findViewById(R.id.tvRestaurantName)
        tvReviewDateDetail = findViewById(R.id.tvReviewDateDetail)
        tvTipoComidaDetail = findViewById(R.id.tvTipoComidaDetail)
        tvRangoPrecioDetail = findViewById(R.id.tvRangoPrecioDetail)
        tvUbicacionDetail = findViewById(R.id.tvUbicacionDetail)
        ratingBarDetail = findViewById(R.id.ratingBarDetail)
        progressBar = findViewById(R.id.progressBar)
        tvCommentsTitle = findViewById(R.id.tvCommentsTitle)
        comentariosContainer = findViewById(R.id.comentariosContainer) // Asumimos que tienes un LinearLayout con este ID en tu XML
        tvNoComments = findViewById(R.id.tvNoComments)
        progressBarComments = findViewById(R.id.progressBarComments)

        // El contenedor dinámico para comentarios ya debería estar en tu XML
        // y lo referenciamos aquí con findViewById. No se crea dinámicamente aquí.
    }

    private fun setupViewModels() {
        resenaViewModel = ViewModelProvider(this)[ResenaViewModel::class.java]
        comentarioViewModel = ViewModelProvider(this)[ComentarioViewModel::class.java]
    }

    private fun setupObservers() {
        // Observer para comentarios
        comentarioViewModel.comentarios.observe(this) { comentarios ->
            displayComentarios(comentarios)
        }

        // Observer para errores de comentarios
        comentarioViewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                comentarioViewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        // Cerrar Activity al pulsar la flecha de atrás
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // botón añadir comentario
        findViewById<ImageButton>(R.id.btnAddComment).setOnClickListener {
            val intent = Intent(this, NuevoComentarioActivity::class.java)
            intent.putExtra("resena_id", resenaId)
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
                    finish()
                    true
                }
                // Navegar a la pantalla de populares (PopularesActivity)
                R.id.nav_populares -> {
                    startActivity(Intent(this, PopularesActivity::class.java))
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

    private fun loadReviewDetails() {
        progressBar.visibility = View.VISIBLE

        resenaViewModel.getResenaById(resenaId).observe(this) { resena ->
            progressBar.visibility = View.GONE
            resena?.let {
                displayReviewDetails(it)
            } ?: run {
                Toast.makeText(this, "No se pudo cargar la reseña", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Observar errores
        resenaViewModel.error.observe(this) { error ->
            error?.let {
                progressBar.visibility = View.GONE
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                resenaViewModel.clearError()
            }
        }
    }

    private fun loadComentarios() {
        comentarioViewModel.getComentariosByResenaId(resenaId)
    }

    private fun displayReviewDetails(resena: Resena) {
        // Información principal de la reseña
        tvReviewTitle.text = resena.tituloResena
        tvReviewDescription.text = resena.comentarios

        // Información del restaurante
        tvRestaurantName.text = resena.nombreRestaurante
        tvTipoComidaDetail.text = resena.tipoComida
        tvRangoPrecioDetail.text = resena.rangoPrecio
        tvUbicacionDetail.text = resena.ubicacion
        ratingBarDetail.rating = resena.calificacion

        // Formatear y mostrar fecha
        tvReviewDateDetail.text = formatDate(resena.fechaCreacion)

        // Cargar la primera imagen si existe
        if (resena.imagenes.isNotEmpty()) {
            val imagePaths = resena.imagenes.split(",")
            if (imagePaths.isNotEmpty() && imagePaths[0].isNotEmpty()) {
                loadImageFromPath(imagePaths[0])
            }
        }
    }

    private fun displayComentarios(comentarios: List<Comentario>) {
        // Limpiar el contenedor antes de agregar nuevos comentarios
        comentariosContainer.removeAllViews()

        progressBarComments.visibility = View.GONE

        if (comentarios.isEmpty()) {
            tvNoComments.visibility = View.VISIBLE
            return
        } else {
            tvNoComments.visibility = View.GONE
        }

        // Agregar cada comentario dinámicamente
        comentarios.forEach { comentario ->
            val comentarioView = createComentarioView(comentario)
            comentariosContainer.addView(comentarioView)
        }
    }

    private fun createComentarioView(comentario: Comentario): View {
        val comentarioView = layoutInflater.inflate(R.layout.item_comentario, comentariosContainer, false)

        // Referencias a las vistas
        val tvCommenterName = comentarioView.findViewById<TextView>(R.id.tvCommenterName)
        val tvCommentDate = comentarioView.findViewById<TextView>(R.id.tvCommentDate)
        val tvCommentText = comentarioView.findViewById<TextView>(R.id.tvCommentText)
        val ivAvatarComment = comentarioView.findViewById<ImageView>(R.id.ivAvatarComment)

        // Asignar los datos
        tvCommenterName.text = comentario.autorNombre
        tvCommentDate.text = formatDateRelative(comentario.fechaCreacion)
        tvCommentText.text = comentario.contenido

        // Avatar por defecto (puedes personalizarlo más adelante)
        ivAvatarComment.setImageResource(R.drawable.avatar_laura) // Asume que tienes un drawable para el avatar por defecto

        return comentarioView
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun loadImageFromPath(imagePath: String) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                bitmap?.let {
                    ivReviewImage.setImageBitmap(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Mantener la imagen por defecto si hay error o cargar un placeholder
            ivReviewImage.setImageResource(R.drawable.avatar_alejandro) // Asume un placeholder
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatDateRelative(dateString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = format.parse(dateString)
            val now = Date()
            val diffInMillis = now.time - (date?.time ?: 0)
            val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

            when {
                diffInDays == 0L -> "Hoy"
                diffInDays == 1L -> "Ayer"
                diffInDays < 7 -> "Hace $diffInDays días"
                else -> {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    date?.let { outputFormat.format(it) } ?: dateString
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }
}