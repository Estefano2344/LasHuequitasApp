package com.proano.estefano.lashuequitasapp.view

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.SessionManager
import com.proano.estefano.lashuequitasapp.viewmodel.NuevaResenaViewModel
import com.proano.estefano.lashuequitasapp.viewmodel.SaveResenaResult

class NuevaResenaActivity : AppCompatActivity() {

    // ViewModel
    private lateinit var viewModel: NuevaResenaViewModel
    private lateinit var sessionManager: SessionManager

    // Definir las vistas
    private lateinit var nombreRestaurante: EditText
    private lateinit var ubicacion: EditText
    private lateinit var rangoPrecio: Spinner
    private lateinit var tipoComida: Spinner
    private lateinit var tituloResena: EditText
    private lateinit var btnSubir: Button
    private lateinit var imagePreview: ImageView
    private lateinit var textImagen: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var comentariosResena: EditText
    private lateinit var recyclerViewImages: RecyclerView

    // Botones para cámara y galería
    private lateinit var btnCamara: Button
    private lateinit var btnGaleria: Button

    // Adaptador para las imágenes
    private lateinit var imageAdapter: ImageAdapter

    // Constantes para permisos y solicitudes
    private val CAMERA_PERMISSION_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST = 101 // Usado para READ_EXTERNAL_STORAGE o READ_MEDIA_IMAGES

    // Variables para las imágenes
    private var imageUri: Uri? = null // Para almacenar la URI de la imagen capturada por la cámara

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_resena)

        // Inicializar ViewModel y SessionManager
        viewModel = ViewModelProvider(this)[NuevaResenaViewModel::class.java]
        sessionManager = SessionManager(this)

        // Verificar si el usuario está logueado
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        // Ajuste de inset para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        initViews()

        // Configurar observadores
        setupObservers()

        // Configurar listeners
        setupListeners()

        // Configurar Spinners
        setupSpinners()

        // Configurar RecyclerView
        setupRecyclerView()
    }

    private fun initViews() {
        nombreRestaurante = findViewById(R.id.nombreRestaurante)
        ubicacion = findViewById(R.id.ubicacion)
        rangoPrecio = findViewById(R.id.rango_precio)
        tipoComida = findViewById(R.id.tipo_comida)
        tituloResena = findViewById(R.id.tituloResenaEdit)
        btnSubir = findViewById(R.id.btnSubir)
        imagePreview = findViewById(R.id.imagePreview)
        textImagen = findViewById(R.id.textImagen)
        ratingBar = findViewById(R.id.ratingBar)
        comentariosResena = findViewById(R.id.comentariosResena)
        btnCamara = findViewById(R.id.btnCamara)
        btnGaleria = findViewById(R.id.btnGaleria)
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        // Asegúrate de que el ID `main` exista en tu layout XML raíz para el ajuste de insets.
        // Si no existe, reemplázalo con el ID de tu vista raíz.
    }

    private fun setupObservers() {
        // Observar el resultado del guardado
        viewModel.saveResult.observe(this) { result ->
            when (result) {
                is SaveResenaResult.Success -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    clearFields()
                    // Opcional: navegar de vuelta al home
                    navigateToHome()
                }
                is SaveResenaResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observar el estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            btnSubir.isEnabled = !isLoading
            if (isLoading) {
                btnSubir.text = "Guardando..."
            } else {
                btnSubir.text = getString(R.string.btn_subir)
            }
        }

        // Observar la lista de imágenes
        viewModel.imagesList.observe(this) { images ->
            imageAdapter.updateImages(images)
            updateImageVisibility(images)
        }
    }

    private fun setupListeners() {
        // Listener para el botón de guardar
        btnSubir.setOnClickListener {
            guardarResena()
        }

        // Listeners para botones de imagen
        btnCamara.setOnClickListener {
            checkCameraPermission()
        }

        btnGaleria.setOnClickListener {
            checkStoragePermission()
        }

        // Cerrar la actividad
        findViewById<ImageView>(R.id.closeProfile).setOnClickListener {
            finish()
        }

        // Bottom Navigation
        setupBottomNavigation()
    }

    private fun setupSpinners() {
        // Configurar Spinner para "Rango de Precio"
        val rangoAdapter = ArrayAdapter.createFromResource(
            this, R.array.rango_precio, android.R.layout.simple_spinner_item
        )
        rangoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangoPrecio.adapter = rangoAdapter

        // Configurar Spinner para "Tipo de Comida"
        val tipoAdapter = ArrayAdapter.createFromResource(
            this, R.array.tipo_comida, android.R.layout.simple_spinner_item
        )
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoComida.adapter = tipoAdapter
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(mutableListOf()) { position ->
            viewModel.removeImage(position)
        }
        recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewImages.adapter = imageAdapter
    }

    private fun guardarResena() {
        val nombre = nombreRestaurante.text.toString()
        val ubicacionTexto = ubicacion.text.toString()
        val titulo = tituloResena.text.toString()
        val comentarios = comentariosResena.text.toString()
        val calificacion = ratingBar.rating
        val rango = rangoPrecio.selectedItem.toString()
        val tipo = tipoComida.selectedItem.toString()
        val images = viewModel.imagesList.value ?: mutableListOf()

        viewModel.guardarResena(
            nombreRestaurante = nombre,
            ubicacion = ubicacionTexto,
            rangoPrecio = rango,
            tipoComida = tipo,
            calificacion = calificacion,
            tituloResena = titulo,
            comentarios = comentarios,
            imageUris = images
        )
    }

    private fun updateImageVisibility(images: List<Uri>) {
        if (images.isNotEmpty()) {
            // Mostrar la primera imagen en el preview
            imagePreview.visibility = View.VISIBLE
            imagePreview.setImageURI(images.first())
            textImagen.visibility = View.GONE

            // Mostrar RecyclerView si hay más de una imagen
            if (images.size > 1) {
                recyclerViewImages.visibility = View.VISIBLE
            } else {
                recyclerViewImages.visibility = View.GONE
            }
        } else {
            imagePreview.visibility = View.GONE
            textImagen.visibility = View.VISIBLE
            recyclerViewImages.visibility = View.GONE
        }
    }

    // === MÉTODOS DE PERMISOS Y CÁMARA ===
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            openCamera()
        }
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13+ usamos READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    STORAGE_PERMISSION_REQUEST
                )
            } else {
                openGallery()
            }
        } else {
            // Para versiones anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST
                )
            } else {
                openGallery()
            }
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        values.put(MediaStore.Images.Media.DESCRIPTION, "De la cámara")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Launcher para la cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri?.let {
                viewModel.addImage(it)
            }
        }
    }

    // Launcher para la galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                viewModel.addImage(uri)
            }
        }
    }

    // Manejar respuestas de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Se requiere permiso de almacenamiento", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    navigateToHome()
                    true
                }
                R.id.nav_populares -> {
                    val intent = Intent(this, PopularesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
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
    }

    private fun clearFields() {
        nombreRestaurante.text.clear()
        ubicacion.text.clear()
        tituloResena.text.clear()
        comentariosResena.text.clear()
        ratingBar.rating = 0f
        rangoPrecio.setSelection(0)
        tipoComida.setSelection(0)
        viewModel.clearImages()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Usar onBackPressedDispatcher para API 33+
        return true
    }
}