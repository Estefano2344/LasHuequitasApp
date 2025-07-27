package com.proano.estefano.lashuequitasapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.proano.estefano.lashuequitasapp.R
import com.proano.estefano.lashuequitasapp.model.Favorito

class FavoritosAdapter(private val favoritos: List<Favorito>) :
    RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

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
        // Si `imagenes` es una ruta o nombre de recurso, aquí debes cargar la imagen correctamente.
        // Si es un nombre de recurso drawable:
        val context = holder.imgRestaurante.context
        val resId = context.resources.getIdentifier(favorito.imagenes, "drawable", context.packageName)
        if (resId != 0) {
            holder.imgRestaurante.setImageResource(resId)
        } else {
            holder.imgRestaurante.setImageResource(R.drawable.ic_launcher_background) // Imagen por defecto
        }
        holder.nombreRestaurante.text = favorito.nombre
        holder.puntuacion.text = favorito.puntuacion.toString()
        holder.cantidadResenas.text = "(${favorito.comentarios} reseñas)"
    }

    override fun getItemCount() = favoritos.size
}