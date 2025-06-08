package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

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
    }
}