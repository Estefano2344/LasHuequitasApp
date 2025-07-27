// src/main/java/com/proano/estefano/lashuequitasapp/view/RecommendedRestaurantsAdapter.kt
package com.proano.estefano.lashuequitasapp.view

import android.content.Intent
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

class RecommendedRestaurantsAdapter(private var restaurants: List<Restaurant>) :
    RecyclerView.Adapter<RecommendedRestaurantsAdapter.RecommendedRestaurantViewHolder>() {

    class RecommendedRestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.restaurantImageView) // Asumiendo estos IDs en item_recommended_restaurant.xml
        val nameTextView: TextView = view.findViewById(R.id.restaurantNameTextView)
        val ratingTextView: TextView = view.findViewById(R.id.restaurantRatingTextView)
        val reviewCountTextView: TextView = view.findViewById(R.id.restaurantReviewCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedRestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_restaurant, parent, false) // Reutilizamos un layout similar o uno nuevo si es necesario
        return RecommendedRestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendedRestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        holder.nameTextView.text = restaurant.name
        holder.ratingTextView.text = holder.itemView.context.getString(R.string.puntuacion_format, restaurant.rating)
        holder.reviewCountTextView.text = holder.itemView.context.getString(R.string.resenas_format, restaurant.reviewCount)

        // Cargar imagen usando Glide
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

        // Listener para el click en el item recomendado
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra("restaurant_data", restaurant)
            context.startActivity(intent)
            // Opcional: Si quieres que al hacer clic en una recomendación se reemplace la actividad actual de detalle,
            // puedes añadir context.startActivity(intent) y luego (context as? Activity)?.finish() si context es Activity
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateData(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }
}