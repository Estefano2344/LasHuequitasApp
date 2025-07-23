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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proano.estefano.lashuequitasapp.R

class NuevaResenaActivity : AppCompatActivity() {

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

    // Constantes para permisos y solicitudes
    private val CAMERA_PERMISSION_REQUEST = 100
    private val STORAGE_PERMISSION_REQUEST = 101

    // Variables para las imágenes
    private var imageUri: Uri? = null
    private val imagesList = ArrayList<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_resena)

        // Ajuste de inset para edge-to-edge sobre el root correcto
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración de vistas
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

        // Configurar RecyclerView para múltiples imágenes
        recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Aquí necesitarás crear un adaptador personalizado para las imágenes
        // recyclerViewImages.adapter = ImageAdapter(imagesList)

        // Configurar Spinners para "Rango de Precio" y "Tipo de Comida"
        val rangoAdapter = ArrayAdapter.createFromResource(
            this, R.array.rango_precio, android.R.layout.simple_spinner_item
        )
        rangoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangoPrecio.adapter = rangoAdapter

        val tipoAdapter = ArrayAdapter.createFromResource(
            this, R.array.tipo_comida, android.R.layout.simple_spinner_item
        )
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoComida.adapter = tipoAdapter

        // Configurar botón de cámara
        btnCamara.setOnClickListener {
            checkCameraPermission()
        }

        // Configurar botón de galería
        btnGaleria.setOnClickListener {
            checkStoragePermission()
        }

        // Manejar la acción de "Subir"
        btnSubir.setOnClickListener {
            val nombre = nombreRestaurante.text.toString()
            val ubicacionTexto = ubicacion.text.toString()
            val titulo = tituloResena.text.toString()
            val comentarios = comentariosResena.text.toString()
            val calificacion = ratingBar.rating

            if (nombre.isEmpty() || ubicacionTexto.isEmpty() || titulo.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener el rango de precio y tipo de comida seleccionados
            val rango = rangoPrecio.selectedItem.toString()
            val tipo = tipoComida.selectedItem.toString()

            // Aquí puedes guardar los datos o realizar otra acción
            // También puedes usar la calificación (calificacion) y las imágenes (imagesList)
            Toast.makeText(this, "Reseña guardada con éxito", Toast.LENGTH_SHORT).show()

            // Volver a la actividad anterior o limpiar los campos
            clearFields()
        }

        // Cerrar la actividad cuando se haga clic en el botón de retroceso
        findViewById<ImageView>(R.id.closeProfile).setOnClickListener {
            finish()
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
                    val intent = Intent(this, PopularesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                // Navegar a la pantalla de favoritos (FavoritosActivity)
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                // Navegar a la pantalla del perfil (PerfilActivity)
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

    // Comprobar y solicitar permisos de cámara
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            openCamera()
        }
    }

    // Comprobar y solicitar permisos de almacenamiento
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Para Android 13+ usamos READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) !=
                PackageManager.PERMISSION_GRANTED) {
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
                PackageManager.PERMISSION_GRANTED) {
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

    // Abrir la cámara
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
        values.put(MediaStore.Images.Media.DESCRIPTION, "De la cámara")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraLauncher.launch(cameraIntent)
    }

    // Abrir la galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    // Launcher para la cámara
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri?.let {
                addImageToCollection(it)
            }
        }
    }

    // Launcher para la galería
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                addImageToCollection(uri)
            }
        }
    }

    // Añadir imagen a la colección
    private fun addImageToCollection(uri: Uri) {
        imagesList.add(uri)

        // Mostrar la vista previa
        imagePreview.visibility = View.VISIBLE
        imagePreview.setImageURI(uri)

        // Ocultar el texto informativo
        textImagen.visibility = View.GONE

        // Si hay más de una imagen, mostrar el RecyclerView
        if (imagesList.size > 1) {
            recyclerViewImages.visibility = View.VISIBLE
            // Notificar al adaptador sobre cambios
            // (adapter as? ImageAdapter)?.notifyDataSetChanged()
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

    // Limpiar los campos después de subir la reseña
    private fun clearFields() {
        nombreRestaurante.text.clear()
        ubicacion.text.clear()
        tituloResena.text.clear()
        comentariosResena.text.clear()
        ratingBar.rating = 0f
        imagesList.clear()
        imagePreview.visibility = View.GONE
        textImagen.visibility = View.VISIBLE
        recyclerViewImages.visibility = View.GONE
    }

    // Retroceso en la barra de acciones
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}