package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.proano.estefano.lashuequitasapp.R

class PantallaInicioDeSesionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_inicio_de_sesion)

        findViewById<TextView>(R.id.textoOlvidasteC).setOnClickListener {
            startActivity(Intent(this, RecuperarContrasenaActivity::class.java))
        }

        findViewById<TextView>(R.id.textoCrearCuenta).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        val btnLogin = findViewById<Button>(R.id.botonIniciarSesion)
        btnLogin.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}