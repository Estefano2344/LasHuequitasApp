package com.proano.estefano.lashuequitasapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.businesslogic.UserRepository
import com.proano.estefano.lashuequitasapp.model.entities.Resena
import java.io.File

class ReviewAdapter(
    private var resenas: List<Resena> = emptyList(),
    private val onViewReviewClick: (Resena) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAuthorAvatar: ImageView = itemView.findViewById(R.id.ivAuthorAvatar)
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
            val userRepo = UserRepository(itemView.context)
            val autor = userRepo.getUserById(resena.autorId)

            if (autor?.foto != null && autor.foto!!.isNotEmpty()) {
                val file = File(autor.foto!!)
                if (file.exists()) {
                    Glide.with(itemView.context)
                        .load(file)
                        .placeholder(R.drawable.avatar_julia)
                        .circleCrop()
                        .into(ivAuthorAvatar)
                } else {
                    Glide.with(itemView.context)
                        .load(autor.foto)
                        .placeholder(R.drawable.avatar_julia)
                        .circleCrop()
                        .into(ivAuthorAvatar)
                }
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.avatar_julia)
                    .circleCrop()
                    .into(ivAuthorAvatar)
            }

            tvReviewTitle.text = resena.tituloResena
            tvReviewDate.text = formatearFecha(resena.fechaCreacion)
            ratingBar.rating = resena.calificacion
            tvReviewText.text = if (resena.comentarios.length > 100) {
                "${resena.comentarios.take(100)}..."
            } else {
                resena.comentarios
            }
            tvTipoComida.text = resena.tipoComida
            tvRangoPrecio.text = resena.rangoPrecio
            tvUbicacion.text = resena.ubicacion
            btnViewFullReview.setOnClickListener { onViewReviewClick(resena) }
            itemView.setOnClickListener { onViewReviewClick(resena) }
        }
    }

    override fun getItemCount(): Int = resenas.size

    fun updateResenas(nuevasResenas: List<Resena>) {
        resenas = nuevasResenas
        notifyDataSetChanged()
    }

    private fun formatearFecha(fecha: String): String {
        return fecha
    }
}