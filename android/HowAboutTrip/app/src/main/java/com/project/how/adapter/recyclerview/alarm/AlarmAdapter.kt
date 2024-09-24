package com.project.how.adapter.recyclerview.alarm

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.how.data_class.roomdb.Alarm
import com.project.how.databinding.AlarmItemBinding
import com.project.how.di.entrypoint.AlarmReceiverEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.internal.aggregatedroot.codegen._com_project_how_Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat

class AlarmAdapter(private val context: Context, alarms : List<Alarm>) : RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {
    private val data = alarms.toMutableList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd a HH:mm")
    inner class ViewHolder(val binding : AlarmItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data : Alarm, position: Int){
            if (data.isChecked){
                binding.check.visibility = View.GONE
            }else{
                binding.check.visibility = View.VISIBLE
            }

            binding.text.text = data.text
            binding.date.text = dateFormat.format(data.createdAt)

            itemView.setOnClickListener {
                if (!data.isChecked){
                    CoroutineScope(Dispatchers.IO).launch {
                        val entryPoint = EntryPointAccessors.fromApplication(
                            context,
                            AlarmReceiverEntryPoint::class.java
                        )
                        val alarmDao = entryPoint.alarmDao()
                        alarmDao.check(data.id, true)
                        data.isChecked = true
                        withContext(Dispatchers.Main){
                            notifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        AlarmItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount(): Int = data.size
    override fun getItemViewType(position: Int): Int = position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    fun add(newData : MutableList<Alarm>){
        data.addAll(newData)
        notifyDataSetChanged()
    }
}