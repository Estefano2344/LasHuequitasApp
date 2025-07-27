// src/main/java/com/proano/estefano/lashuequitasapp/view/RestaurantSearchAdapter.kt
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
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant

class RestaurantSearchAdapter(private var restaurants: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantSearchAdapter.RestaurantViewHolder>() {

    class RestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.restaurantImageView)
        val nameTextView: TextView = view.findViewById(R.id.restaurantNameTextView)
        val ratingTextView: TextView = view.findViewById(R.id.restaurantRatingTextView)
        val reviewCountTextView: TextView = view.findViewById(R.id.restaurantReviewCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant_card, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.nameTextView.text = restaurant.name
        holder.ratingTextView.text = holder.itemView.context.getString(R.string.puntuacion_format, restaurant.rating)
        holder.reviewCountTextView.text = holder.itemView.context.getString(R.string.resenas_format, restaurant.reviewCount)

        // Cargar imagen usando Glide, similar a HomeActivity
        if (restaurant.imageUrl.startsWith("content://") || restaurant.imageUrl.startsWith("file://")) {
            Glide.with(holder.itemView.context)
                .load(Uri.parse(restaurant.imageUrl))
                .placeholder(R.drawable.placeholder_restaurant)
                .error(R.drawable.placeholder_restaurant)
                .into(holder.imageView)
        } else {
            val imageResId = holder.itemView.context.resources.getIdentifier(restaurant.imageUrl, "drawable", holder.itemView.context.packageName)
            if (imageResId != 0) {
                Glide.with(holder.itemView.context)
                    .load(imageResId)
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_restaurant)
            }
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateData(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}