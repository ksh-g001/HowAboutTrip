package com.project.how.adapter.recyclerview.record.image

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.how.data_class.dto.recode.image.ImageElement
import com.project.how.data_class.dto.recode.image.ImagesResponse
import com.project.how.databinding.GalleryItemBinding
import java.time.LocalDateTime

class GalleryAdapter(
    images : ImagesResponse,
    private val onItemClickListener: ImageAdapter.OnItemClickListener
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    private var datas : MutableMap<String, List<ImageElement>> = images.scheduleImages.groupBy { it.date.split("T")[0] }.toMutableMap()
    private var keys = datas.keys.toMutableList()
    private var edit = false
    init {
        keys.sort()
        Log.d("GalleryAdapter", "keys : $keys")
    }

    inner class ViewHolder(val binding : GalleryItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(date : String, images : List<ImageElement>){
            val adapter = ImageAdapter(images.toMutableList(), onItemClickListener)
            binding.gallery.adapter = adapter
            binding.date.text = date
            if (edit)
                ableEdit(adapter)
            else
                unableEdit(adapter)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        GalleryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val images = datas[keys[position]]
        if (images != null) {
            holder.bind(keys[position], images)
        }
    }

    fun update(images : ImagesResponse){
        datas = images.scheduleImages.groupBy { it.date.split("T")[0] }.toMutableMap()
        keys = datas.keys.toMutableList()
        Log.d("GalleryAdapter", "keys : $keys")
        notifyDataSetChanged()
    }

    fun editStart(){
        edit = true
        notifyDataSetChanged()
    }

    fun editEnd(){
        edit = false
        notifyDataSetChanged()
    }

    fun getEdit() : Boolean = edit

    fun add(newData : ImageElement){
        val date = newData.date.split(" ", "T")[0]
        Log.d("GalleryAdapter", "date : $date")
        if (keys.contains(date)){
            val d = datas[date]
            datas[date] = d!!.plus(newData)
            notifyItemChanged(keys.indexOf(date))
            return
        }

        datas[date] = listOf(newData)
        keys.add(date)
        keys.sort()
        notifyItemRangeChanged(keys.indexOf(date), keys.size)
    }

    private fun ableEdit(adapter: ImageAdapter){
        adapter.editStart()
    }

    private fun unableEdit(adapter: ImageAdapter){
        adapter.editEnd()
    }
}