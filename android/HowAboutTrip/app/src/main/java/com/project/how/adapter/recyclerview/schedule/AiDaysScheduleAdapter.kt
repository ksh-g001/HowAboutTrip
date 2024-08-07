package com.project.how.adapter.recyclerview.schedule

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.how.R
import com.project.how.data_class.recyclerview.AiDaysSchedule
import com.project.how.databinding.AiDaysScheduleItemBinding

class AiDaysScheduleAdapter(private val context: Context, data: List<AiDaysSchedule>)
    : RecyclerView.Adapter<AiDaysScheduleAdapter.ViewHolder>(){
        private var dailySchedule = data

        inner class ViewHolder(val binding: AiDaysScheduleItemBinding) : RecyclerView.ViewHolder(binding.root){
            fun bind(data : AiDaysSchedule, position: Int){
                binding.scheduleTitle.text = data.places
                binding.budget.text = context.getString(R.string.budget, data.budget.toString())
                when (position) {
                    0 -> binding.topDottedLine.visibility = View.GONE
                    dailySchedule.lastIndex -> binding.bottomDottedLine.visibility = View.GONE
                    else -> {
                        binding.topDottedLine.visibility = View.VISIBLE
                        binding.bottomDottedLine.visibility = View.VISIBLE
                    }
                }

                when(data.type){
                    AIRPLANE ->{
                        binding.search.visibility = View.VISIBLE
                        binding.number.visibility = View.GONE
                        binding.numberBackground.setImageResource(R.drawable.icon_airplane)
                    }
                    HOTEL ->{
                        binding.number.text = "${position+1}"
                        binding.number.visibility = View.VISIBLE
                        binding.search.visibility = View.VISIBLE
                        binding.numberBackground.setImageResource(R.drawable.white_oval)

                    }
                    PLACE ->{
                        binding.number.text = "${position+1}"
                        binding.number.visibility = View.VISIBLE
                        binding.search.visibility = View.GONE
                        binding.numberBackground.setImageResource(R.drawable.white_oval)
                    }
                }
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        AiDaysScheduleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))

    override fun getItemCount(): Int = dailySchedule.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dailySchedule[position]
        holder.bind(data, position)
    }

    override fun getItemViewType(position: Int): Int = position

    fun update(data : List<AiDaysSchedule>){
        dailySchedule = data
        notifyDataSetChanged()
    }

    companion object{
        const val AIRPLANE = 0
        const val HOTEL = 1
        const val PLACE = 2
    }
}