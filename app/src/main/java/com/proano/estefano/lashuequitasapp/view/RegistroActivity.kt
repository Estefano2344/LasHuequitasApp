package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.viewmodel.RegistroResult
import com.proano.estefano.lashuequitasapp.viewmodel.RegistroViewModel

class RegistroActivity : AppCompatActivity() {

    private lateinit var viewModel: RegistroViewModel

    // Referencias a las vistas
    private lateinit var nombreEditText: TextInputEditText
    private lateinit var apellidoEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var usuarioEditText: TextInputEditText
    private lateinit var preferenciasChipGroup: ChipGroup
    private lateinit var registrarmeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[RegistroViewModel::class.java]

        // Inicializar vistas
        initViews()

        // Configurar observadores
        setupObservers()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        nombreEditText = findViewById(R.id.nombreEditText)
        apellidoEditText = findViewById(R.id.apellidoEditText)
        emailEditText = findViewById(R.id.campoEmailRC)
        passwordEditText = findViewById(R.id.campoContraseña)
        usuarioEditText = findViewById(R.id.campoUsuarioEmail)
        preferenciasChipGroup = findViewById(R.id.preferenciasChipGroup)
        registrarmeButton = findViewById(R.id.registrarmeButton)
    }

    private fun setupObservers() {
        // Observar el resultado del registro
        viewModel.registroResult.observe(this) { result ->
            when (result) {
                is RegistroResult.Success -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    // Navegar a la pantalla de inicio de sesión
                    val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                is RegistroResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observar el estado de carga
        viewModel.isLoading.observe(this) { isLoading ->
            registrarmeButton.isEnabled = !isLoading
            if (isLoading) {
                registrarmeButton.text = "Registrando..."
            } else {
                registrarmeButton.text = getString(R.string.registrarme)
            }
        }
    }

    private fun setupListeners() {
        // Listener para el botón Registrarme
        registrarmeButton.setOnClickListener {
            val nombre = nombreEditText.text.toString()
            val apellido = apellidoEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val usuario = usuarioEditText.text.toString()
            val preferencias = getSelectedPreferences()

            viewModel.registrarUsuario(nombre, apellido, email, password, usuario, preferencias)
        }

        // Listener para el icono de cerrar
        findViewById<ImageView>(R.id.cerrarImageView).setOnClickListener {
            val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun getSelectedPreferences(): List<String> {
        val selectedPreferences = mutableListOf<String>()

        for (i in 0 until preferenciasChipGroup.childCount) {
            val chip = preferenciasChipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                selectedPreferences.add(chip.text.toString())
            }
        }

        return selectedPreferences
    }
}