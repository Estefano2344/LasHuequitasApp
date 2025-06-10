package com.proano.estefano.lashuequitasapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RestaurantReviewsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_restaurant_reviews)

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1) Flecha de volver
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 2) Botones “Ver reseña” → ReviewDetailsActivity
        findViewById<MaterialButton>(R.id.btnViewReview1).setOnClickListener {
            startActivity(Intent(this, ReviewDetailsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnViewReview2).setOnClickListener {
            startActivity(Intent(this, ReviewDetailsActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnViewReview3).setOnClickListener {
            startActivity(Intent(this, ReviewDetailsActivity::class.java))
        }
    }
}
