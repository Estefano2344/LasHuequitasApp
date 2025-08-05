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
import com.proano.estefano.lashuequitasapp.model.businesslogic.ResenaRepository
import com.proano.estefano.lashuequitasapp.model.entities.Restaurant
import java.io.File

class RecommendedRestaurantsAdapter(
    private var restaurants: List<Restaurant>,
    private val resenaRepository: ResenaRepository
) : RecyclerView.Adapter<RecommendedRestaurantsAdapter.RecommendedRestaurantViewHolder>() {

    class RecommendedRestaurantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.restaurantImageView)
        val nameTextView: TextView = view.findViewById(R.id.restaurantNameTextView)
        val ratingTextView: TextView = view.findViewById(R.id.restaurantRatingTextView)
        val reviewCountTextView: TextView = view.findViewById(R.id.restaurantReviewCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedRestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommended_restaurant, parent, false)
        return RecommendedRestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendedRestaurantViewHolder, position: Int) {
        val restaurant = restaurants[position]
        val context = holder.itemView.context

        holder.nameTextView.text = restaurant.name
        holder.ratingTextView.text = context.getString(R.string.puntuacion_format, restaurant.rating)
        holder.reviewCountTextView.text = context.getString(R.string.resenas_format, restaurant.reviewCount)

        val latestImagePath = resenaRepository.getLatestRestaurantImage(restaurant.name)

        if (!latestImagePath.isNullOrEmpty()) {
            val imageFile = File(latestImagePath)
            if (imageFile.exists()) {
                Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(holder.imageView)
            } else {
                loadDefaultImage(context, restaurant, holder.imageView)
            }
        } else {
            loadDefaultImage(context, restaurant, holder.imageView)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RestaurantDetailActivity::class.java)
            intent.putExtra("restaurant_data", restaurant)
            context.startActivity(intent)
        }
    }

    private fun loadDefaultImage(context: android.content.Context, restaurant: Restaurant, imageView: ImageView) {
        if (restaurant.imageUrl.startsWith("content://") || restaurant.imageUrl.startsWith("file://")) {
            Glide.with(context)
                .load(Uri.parse(restaurant.imageUrl))
                .placeholder(R.drawable.placeholder_restaurant)
                .error(R.drawable.placeholder_restaurant)
                .into(imageView)
        } else {
            val imageResId = context.resources.getIdentifier(restaurant.imageUrl, "drawable", context.packageName)
            if (imageResId != 0) {
                Glide.with(context)
                    .load(imageResId)
                    .placeholder(R.drawable.placeholder_restaurant)
                    .error(R.drawable.placeholder_restaurant)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.placeholder_restaurant)
            }
        }
    }

    override fun getItemCount(): Int = restaurants.size

    fun updateData(newRestaurants: List<Restaurant>) {
        restaurants = newRestaurants
        notifyDataSetChanged()
    }
}