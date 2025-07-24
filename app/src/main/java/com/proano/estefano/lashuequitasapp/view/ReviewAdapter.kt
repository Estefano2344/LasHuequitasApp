package com.proano.estefano.lashuequitasapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.entities.Resena

class ReviewAdapter(
    private var resenas: List<Resena> = emptyList(),
    private val onViewReviewClick: (Resena) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReviewTitle: TextView = itemView.findViewById(R.id.tvReviewTitle)
        val tvReviewDate: TextView = itemView.findViewById(R.id.tvReviewDate)
        val ratingBar: AppCompatRatingBar = itemView.findViewById(R.id.ratingBar)
        val tvReviewText: TextView = itemView.findViewById(R.id.tvReviewText)
        val tvTipoComida: TextView = itemView.findViewById(R.id.tvTipoComida)
        val tvRangoPrecio: TextView = itemView.findViewById(R.id.tvRangoPrecio)
        val tvUbicacion: TextView = itemView.findViewById(R.id.tvUbicacion)
        val btnViewFullReview: MaterialButton = itemView.findViewById(R.id.btnViewFullReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val resena = resenas[position]

        with(holder) {
            // Título de la reseña
            tvReviewTitle.text = resena.tituloResena

            // Fecha de creación
            tvReviewDate.text = formatearFecha(resena.fechaCreacion)

            // Calificación
            ratingBar.rating = resena.calificacion

            // Comentario (limitado para preview)
            tvReviewText.text = if (resena.comentarios.length > 100) {
                "${resena.comentarios.take(100)}..."
            } else {
                resena.comentarios
            }

            // Información adicional
            tvTipoComida.text = resena.tipoComida
            tvRangoPrecio.text = resena.rangoPrecio
            tvUbicacion.text = resena.ubicacion

            // Click listener para ver reseña completa
            btnViewFullReview.setOnClickListener {
                onViewReviewClick(resena)
            }

            // También puedes hacer click en toda la tarjeta
            itemView.setOnClickListener {
                onViewReviewClick(resena)
            }
        }
    }

    override fun getItemCount(): Int = resenas.size

    // Método para actualizar las reseñas
    fun updateResenas(nuevasResenas: List<Resena>) {
        resenas = nuevasResenas
        notifyDataSetChanged()
    }

    // Método auxiliar para formatear fecha
    private fun formatearFecha(fecha: String): String {
        // Aquí puedes implementar la lógica para formatear la fecha
        // Por ejemplo, convertir "2024-01-15" a "Hace 3 días"
        return try {
            // Implementa tu lógica de formateo aquí
            fecha // Por ahora devuelve la fecha tal como está
        } catch (e: Exception) {
            fecha
        }
    }
}