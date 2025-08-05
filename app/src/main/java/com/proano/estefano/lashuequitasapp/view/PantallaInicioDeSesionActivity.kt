package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.viewmodel.LoginResult
import com.proano.estefano.lashuequitasapp.viewmodel.LoginViewModel

class PantallaInicioDeSesionActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio_de_sesion)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        if (viewModel.isUserLoggedIn()) {
            navigateToHome()
            return
        }

        initViews()
        setupObservers()
        setupListeners()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.campoUsuarioEmail)
        passwordEditText = findViewById(R.id.campoContraseña)
        loginButton = findViewById(R.id.botonIniciarSesion)
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.Success -> {
                    Toast.makeText(this, "¡Bienvenido ${result.user.nombre}!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is LoginResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            loginButton.isEnabled = !isLoading
            if (isLoading) {
                loginButton.text = "Iniciando sesión..."
            } else {
                loginButton.text = getString(R.string.textoBotonIniciarSesion)
            }
        }
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            val emailOrUsername = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(emailOrUsername, password)
        }
        findViewById<TextView>(R.id.textoOlvidasteC).setOnClickListener {
            startActivity(Intent(this, RecuperarContrasenaActivity::class.java))
        }
        findViewById<TextView>(R.id.textoCrearCuenta).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}