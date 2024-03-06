package com.project.how.adapter.recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.project.how.BuildConfig
import com.project.how.R
import com.project.how.data_class.dto.GetScheduleListResponse
import com.project.how.data_class.dto.GetScheduleListResponseElement
import com.project.how.databinding.CalendarListItemBinding
import java.text.NumberFormat
import java.util.Locale

class CalendarListAdapter (private val context: Context, data : GetScheduleListResponse, private val onButtonClickListener: OnCalendarListButtonClickListener)
    : RecyclerView.Adapter<CalendarListAdapter.ViewHolder>() {
        private val calendars = data.toMutableList()

    inner class ViewHolder(val binding: CalendarListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : GetScheduleListResponseElement, position: Int){
            val formattedNumber = NumberFormat.getNumberInstance(Locale.getDefault()).format(data.totalPrice)
            binding.budget.text = context.getString(R.string.calendar_budget, formattedNumber)
            binding.title.text = data.scheduleName
            binding.date.text = "${data.startDate} - ${data.endDate}"
            Log.d("CalendarListAdapter", "$position\n${data.imageUrl}")
            Glide.with(binding.root)
                .load(data.imageUrl)
                .error(BuildConfig.ERROR_IMAGE_URl)
                .into(binding.image)

            binding.delete.setOnClickListener {
                onButtonClickListener.onDeleteButtonClickListener(data, position)
            }
            binding.share.setOnClickListener {
                onButtonClickListener.onShareButtonClickListener(data.id)
            }
            binding.checklist.setOnClickListener {
                onButtonClickListener.onCheckListButtonClickListener(data.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        CalendarListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = calendars.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = calendars[position]
        holder.bind(data, position)

        holder.itemView.setOnClickListener {
            val latLng = LatLng(data.latitude, data.longitude)
            onButtonClickListener.onItemClickListener(data.id, latLng)
        }
    }

    fun remove(position: Int){
        calendars.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getData(position: Int) = calendars[position]

    interface OnCalendarListButtonClickListener{
        fun onDeleteButtonClickListener(data : GetScheduleListResponseElement, position: Int)
        fun onItemClickListener(id: Long, latLng: LatLng)
        fun onCheckListButtonClickListener(id: Long)
        fun onShareButtonClickListener(id: Long)
    }
}