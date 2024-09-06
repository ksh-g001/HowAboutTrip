package com.project.how.adapter.recyclerview.record.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.project.how.data_class.dto.recode.image.ImageElement
import com.project.how.databinding.GalleryImageItemBinding

class ImageAdapter(
    private val images :MutableList<ImageElement>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private var edit = false

    inner class ViewHolder(val binding : GalleryImageItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(image : ImageElement, position: Int){
            if (edit)
                binding.delete.visibility = View.VISIBLE
            else
                binding.delete.visibility = View.GONE

            Glide.with(binding.root)
                .load(image.url)
                .preload()

            Glide.with(binding.root)
                .load(image.url)
                .into(binding.imageview)

            binding.delete.setOnClickListener {
                onItemClickListener.onDeleteClickListener(image.id, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        GalleryImageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = images.size

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    fun editStart(){
        edit = true
        notifyDataSetChanged()
    }

    fun editEnd(){
        edit = false
        notifyDataSetChanged()
    }

    fun delete(position: Int){
        images.removeAt(position)
        notifyItemRemoved(position)
    }

    fun add(newImage : ImageElement){
        images.add(newImage)
        notifyItemInserted(images.lastIndex-1)
    }

    interface OnItemClickListener{
        fun onDeleteClickListener(imageId : Long, position: Int)
        fun onItemClickListener(image: String)
    }
}