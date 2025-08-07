package com.proano.estefano.lashuequitasapp.view

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.proano.estefano.lashuequitasapp.R

class ImageAdapter(
    private val imageList: MutableList<Uri>,
    private val onImageRemove: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageItem)
        val removeButton: ImageView? = view.findViewById(R.id.removeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageURI(imageList[position])

        holder.removeButton?.setOnClickListener {
            onImageRemove?.invoke(position)
        }
    }

    override fun getItemCount() = imageList.size

    fun updateImages(newImages: List<Uri>) {
        imageList.clear()
        imageList.addAll(newImages)
        notifyDataSetChanged()
    }
}