package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.net.Uri

class NuevaResenaActivity : AppCompatActivity() {

    // Definir las vistas
    private lateinit var nombreRestaurante: EditText
    private lateinit var ubicacion: EditText
    private lateinit var rangoPrecio: Spinner
    private lateinit var tipoComida: Spinner
    private lateinit var tituloResena: EditText
    private lateinit var btnSubirImagen: LinearLayout
    private lateinit var btnSubir: Button

    // Variable para la imagen seleccionada (aunque no se usará por ahora)
    private var selectedImageUri: Uri? = null


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
        btnSubirImagen = findViewById(R.id.imagenLayout)
        btnSubir = findViewById(R.id.btnSubir)

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

        // Manejar la acción de "Subir Imagen" (solo abre la galería, pero no hace nada con la imagen)
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

            // Aquí puedes guardar los datos o realizar otra acción
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
                    finish()  // Cierra la actividad actual (NuevaResenaActivity)
                    true
                }
                // Navegar a la pantalla de populares (PopularesActivity)
                R.id.nav_populares -> {
                    val intent = Intent(this, PopularesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (NuevaResenaActivity)
                    true
                }
                // Navegar a la pantalla de favoritos (FavoritosActivity)
                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (NuevaResenaActivity)
                    true
                }
                // Navegar a la pantalla del perfil (PerfilActivity)
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()  // Cierra la actividad actual (NuevaResenaActivity)
                    true
                }
                else -> false
            }
        }
    }

    // Limpiar los campos después de subir la reseña
    private fun clearFields() {
        nombreRestaurante.text.clear()
        ubicacion.text.clear()
        tituloResena.text.clear()
        selectedImageUri = null
    }

    // Manejo de resultados de la galería para seleccionar imagen
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            selectedImageUri = data?.data
            selectedImageUri?.let {
                // Aquí podrías manejar la imagen seleccionada (aunque no se usa por ahora)
            }
        }
    }

    // Retroceso en la barra de acciones
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
