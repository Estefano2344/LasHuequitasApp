package com.proano.estefano.lashuequitasapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NuevaResenaActivity : AppCompatActivity() {

    // Definir las vistas
    private lateinit var nombreRestaurante: EditText
    private lateinit var ubicacion: EditText
    private lateinit var rangoPrecio: Spinner
    private lateinit var tipoComida: Spinner
    private lateinit var tituloResena: EditText
    private lateinit var btnSubirImagen: LinearLayout
    private lateinit var btnSubir: Button
    private lateinit var imagenSubida: ImageView
    private lateinit var stars: Array<ImageView>

    // Variable para la imagen seleccionada
    private var selectedImageUri: Uri? = null

    // Manejo de resultados para seleccionar imagen
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            selectedImageUri?.let {
                imagenSubida.setImageURI(it)
                imagenSubida.visibility = ImageView.VISIBLE  // Hacer visible la imagen seleccionada
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nueva_resena)

        // Configurar la barra superior (ActionBar)
        supportActionBar?.apply {
            title = "Nueva Reseña"  // Título de la actividad
            setDisplayHomeAsUpEnabled(true)  // Mostrar el botón de retroceso
        }

        // Vincular vistas
        nombreRestaurante = findViewById(R.id.nombreRestaurante)
        ubicacion = findViewById(R.id.ubicacion)
        rangoPrecio = findViewById(R.id.rango_precio) // Corregido el ID aquí
        tipoComida = findViewById(R.id.tipo_comida) // Corregido el ID aquí
        tituloResena = findViewById(R.id.tituloResenaEdit)
        btnSubirImagen = findViewById(R.id.imagenLayout)  // Corregido el ID aquí
        btnSubir = findViewById(R.id.btnSubir)

        // Estrellas
        stars = arrayOf(
            findViewById(R.id.star1),
            findViewById(R.id.star2),
            findViewById(R.id.star3),
            findViewById(R.id.star4),
            findViewById(R.id.star5)
        )

        // Configurar el Spinner para "Rango de Precio"
        val rangoAdapter = ArrayAdapter.createFromResource(
            this, R.array.rango_precio, android.R.layout.simple_spinner_item
        )
        rangoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rangoPrecio.adapter = rangoAdapter

        // Configurar el Spinner para "Tipo de Comida"
        val tipoAdapter = ArrayAdapter.createFromResource(
            this, R.array.tipo_comida, android.R.layout.simple_spinner_item
        )
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoComida.adapter = tipoAdapter

        // Manejar la acción de "Subir Imagen"
        btnSubirImagen.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }

        // Manejar la acción de "Subir"
        btnSubir.setOnClickListener {
            val nombre = nombreRestaurante.text.toString()
            val ubicacionTexto = ubicacion.text.toString()
            val titulo = tituloResena.text.toString()

            if (nombre.isEmpty() || ubicacionTexto.isEmpty() || titulo.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Obtener el rango de precio y tipo de comida seleccionados
            val rango = rangoPrecio.selectedItem.toString()
            val tipo = tipoComida.selectedItem.toString()

            // Simulando las estrellas de calificación
            var calificacion = 0
            for (i in stars.indices) {
                if (stars[i].alpha == 1f) {
                    calificacion++
                }
            }

            // Aquí puedes guardar los datos o realizar otra acción
            Toast.makeText(this, "Reseña guardada con éxito", Toast.LENGTH_SHORT).show()

            // Volver a la actividad anterior o limpiar los campos
            clearFields()
        }

        // Interacción con las estrellas de calificación
        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                for (i in stars.indices) {
                    stars[i].alpha = if (i <= index) 1f else 0.3f
                }
            }
        }
    }

    // Limpiar los campos después de subir la reseña
    private fun clearFields() {
        nombreRestaurante.text.clear()
        ubicacion.text.clear()
        tituloResena.text.clear()
        selectedImageUri = null
        imagenSubida.setImageURI(null)  // Limpiar la imagen
        imagenSubida.visibility = ImageView.GONE  // Hacer invisible la imagen seleccionada
        stars.forEach { it.alpha = 0.3f }  // Limpiar las estrellas
    }

    // Retroceso en la barra de acciones
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
