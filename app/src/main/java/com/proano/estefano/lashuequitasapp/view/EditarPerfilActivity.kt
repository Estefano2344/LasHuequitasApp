package com.proano.estefano.lashuequitasapp.view

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository
import com.proano.estefano.lashuequitasapp.model.entities.User
import java.io.File
import java.io.FileOutputStream

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private var selectedImagePath: String? = null
    private val REQUEST_CAMERA = 100
    private val REQUEST_GALLERY = 101

    private lateinit var userRepository: UserRepository
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userRepository = UserRepository(this)

        // Recuperar usuario logueado
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = prefs.getLong("user_id", -1)
        currentUser = userRepository.getUserById(userId)

        imagePreview = findViewById(R.id.imagePreview)

        // Precargar datos si existen
        currentUser?.let { user ->
            findViewById<TextInputEditText>(R.id.editNombre).setText(user.nombre)
            findViewById<TextInputEditText>(R.id.editApellido).setText(user.apellido)
            findViewById<TextInputEditText>(R.id.editUsuario).setText(user.usuario)
            findViewById<TextInputEditText>(R.id.editEmail).setText(user.email)

            // Precargar imagen si existe
            user.foto?.let {
                val file = File(it)
                if (file.exists()) {
                    Glide.with(this).load(file).into(imagePreview)
                    imagePreview.visibility = View.VISIBLE
                }
            }
        }

        // Botones para seleccionar imagen
        findViewById<Button>(R.id.btnCamara).setOnClickListener { openCamera() }
        findViewById<Button>(R.id.btnGaleria).setOnClickListener { openGallery() }

        // Botón guardar cambios
        findViewById<Button>(R.id.btnActualizar).setOnClickListener {
            guardarCambios()
        }

        // Cerrar Activity
        findViewById<ImageView>(R.id.closeEditProfile).setOnClickListener { finish() }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data.extras?.get("data") as Bitmap
                    selectedImagePath = saveImageToInternalStorage(photo)
                    imagePreview.setImageBitmap(photo)
                    imagePreview.visibility = View.VISIBLE
                }
                REQUEST_GALLERY -> {
                    val uri = data.data
                    uri?.let {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        selectedImagePath = saveImageToInternalStorage(bitmap)
                        imagePreview.setImageBitmap(bitmap)
                        imagePreview.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val directory = File(filesDir, "profile_images")
        if (!directory.exists()) directory.mkdirs()

        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val file = File(directory, fileName)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        return file.absolutePath
    }

    private fun guardarCambios() {
        currentUser?.let { user ->
            user.nombre = findViewById<TextInputEditText>(R.id.editNombre).text.toString()
            user.apellido = findViewById<TextInputEditText>(R.id.editApellido).text.toString()
            user.usuario = findViewById<TextInputEditText>(R.id.editUsuario).text.toString()
            user.email = findViewById<TextInputEditText>(R.id.editEmail).text.toString()

            // Mantener foto anterior si no se cambió
            if (selectedImagePath != null) {
                user.foto = selectedImagePath
            }

            val actualizado = userRepository.updateUser(user)
            if (actualizado) {
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PerfilActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}