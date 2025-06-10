package com.proano.estefano.lashuequitasapp

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ReviewDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_review_details)

        // Ajuste de inset para edge-to-edge sobre el root correcto
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.detail_root)
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Cerrar Activity al pulsar la flecha de atrás
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }
}
