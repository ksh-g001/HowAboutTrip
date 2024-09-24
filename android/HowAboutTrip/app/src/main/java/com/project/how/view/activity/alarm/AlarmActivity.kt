package com.project.how.view.activity.alarm

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.project.how.R
import com.project.how.adapter.recyclerview.alarm.AlarmAdapter
import com.project.how.databinding.ActivityAlarmBinding
import com.project.how.view_model.AlarmRecordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAlarmBinding
    private val alarmRecordViewModel : AlarmRecordViewModel by viewModels()
    private lateinit var adapter: AlarmAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_alarm)
        binding.alarm = this
        binding.lifecycleOwner = this
        adapter = AlarmAdapter(this, mutableListOf())
        binding.alarmList.adapter = adapter

        lifecycleScope.launch {
            alarmRecordViewModel.init()
        }

        alarmRecordViewModel.lastLiveData.observe(this){
            binding.more.visibility = View.GONE
        }

        alarmRecordViewModel.alarmsLiveData.observe(this){
            adapter.add(it.toMutableList())
        }
    }

    fun loadMore(){
        alarmRecordViewModel.loadMore()
    }
}