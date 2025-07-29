package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.proano.estefano.lashuequitasapp.R
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository

class RecuperarContrasenaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_contrasena)

        val toolbar = findViewById<Toolbar>(R.id.toolbarRecuperarC)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val emailEditText = findViewById<TextInputEditText>(R.id.campoEmailRC)
        val nuevaContrasenaEditText = findViewById<TextInputEditText>(R.id.textoNuevaCon)
        val repetirContrasenaEditText = findViewById<TextInputEditText>(R.id.textoRepetirC)
        val botonActualizar = findViewById<Button>(R.id.botonActualizar)

        botonActualizar.setOnClickListener {
            val email = emailEditText.text.toString()
            val nueva = nuevaContrasenaEditText.text.toString()
            val repetir = repetirContrasenaEditText.text.toString()
            val userRepository = UserRepository(this)
            val resultado = userRepository.recuperarContrasena(email, nueva, repetir)
            Toast.makeText(this, resultado.second, Toast.LENGTH_LONG).show()
            if (resultado.first) {
                val intent = Intent(this, PantallaInicioDeSesionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                emailEditText.text?.clear()
                nuevaContrasenaEditText.text?.clear()
                repetirContrasenaEditText.text?.clear()
            }
        }
    }
}