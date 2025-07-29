package com.proano.estefano.lashuequitasapp.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.Favorito

class FavoritosAdapter(
    private val favoritos: List<Favorito>,
    private val onItemClick: (Favorito) -> Unit
) : RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

    class FavoritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgRestaurante: ImageView = view.findViewById(R.id.imgRestaurante)
        val nombreRestaurante: TextView = view.findViewById(R.id.tvNombreRestaurante)
        val puntuacion: TextView = view.findViewById(R.id.tvPuntuacion)
        val cantidadResenas: TextView = view.findViewById(R.id.tvCantidadResenas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorito, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val favorito = favoritos[position]
        val context = holder.imgRestaurante.context
        val imagenUrl = favorito.imagenUrl

        when {
            imagenUrl.startsWith("content://") || imagenUrl.startsWith("file://") -> {
                Glide.with(context)
                    .load(Uri.parse(imagenUrl))
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(holder.imgRestaurante)
            }
            imagenUrl.startsWith("/") -> {
                Glide.with(context)
                    .load(java.io.File(imagenUrl))
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(holder.imgRestaurante)
            }
            else -> {
                val resId = context.resources.getIdentifier(imagenUrl, "drawable", context.packageName)
                if (resId != 0) {
                    Glide.with(context)
                        .load(resId)
                        .placeholder(R.drawable.placeholder_restaurant)
                        .error(R.drawable.placeholder_restaurant)
                        .into(holder.imgRestaurante)
                } else {
                    holder.imgRestaurante.setImageResource(R.drawable.placeholder_restaurant)
                }
            }
        }

        holder.nombreRestaurante.text = favorito.nombre
        holder.puntuacion.text = favorito.puntuacion.toString()
        holder.cantidadResenas.text = "(${favorito.comentarios} reseñas)"

        holder.itemView.setOnClickListener {
            onItemClick(favorito)
        }
    }

    override fun getItemCount() = favoritos.size
}