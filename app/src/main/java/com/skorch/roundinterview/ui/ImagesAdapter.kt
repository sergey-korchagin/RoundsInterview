package com.skorch.roundinterview.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skorch.imageloader.ImageLoader
import com.skorch.roundinterview.R
import com.skorch.roundinterview.databinding.ImageItemBinding
import com.skorch.roundinterview.domain.ImageData

class ImageAdapter :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private val images = mutableListOf<ImageData>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newImages: List<ImageData>) {
        images.clear()
        images.addAll(newImages)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ImageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(imageData: ImageData) {
            binding.imageId.text = imageData.id.toString()
            ImageLoader.with(binding.root.context).load(
                imageData.imageUrl,
                binding.imageView,
                R.drawable.placeholder
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int = images.size
}
