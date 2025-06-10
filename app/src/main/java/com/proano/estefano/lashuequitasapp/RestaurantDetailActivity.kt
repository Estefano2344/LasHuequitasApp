package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RestaurantDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_detail)

        // Ajuste de inset para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1) Cerrar al pulsar la flecha
        val closeDetail = findViewById<ImageView>(R.id.closeDetail)
        closeDetail.setOnClickListener {
            finish()
        }

        // 2) Ir a RestaurantReviewsActivity
        val btnViewReviews = findViewById<MaterialButton>(R.id.btnViewReviews)
        btnViewReviews.setOnClickListener {
            val intent = Intent(this, RestaurantReviewsActivity::class.java)
            startActivity(intent)
        }
    }
}
