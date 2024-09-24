package com.project.how.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.how.data_class.roomdb.Alarm
import com.project.how.model.AlarmRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class AlarmRecordViewModel @Inject constructor(
    private val alarmRecordRepository: AlarmRecordRepository
) : ViewModel(){
    private var currentPage = 0
    private val _alarmsLiveData = alarmRecordRepository.alarmsLiveData
    private val _lastLiveData = alarmRecordRepository.lastLiveData
    val alarmsLiveData : LiveData<List<Alarm>>
        get() = _alarmsLiveData
    val lastLiveData : LiveData<Boolean>
        get() = _lastLiveData

    init {
        viewModelScope.launch {
            alarmRecordRepository.countLiveData.observeForever {
                currentPage = 0
                getAlarms()
            }
        }
    }


    fun init() = viewModelScope.launch(Dispatchers.IO) {
        getCount()
    }

    private fun getCount() = viewModelScope.launch(Dispatchers.IO) {
        alarmRecordRepository.getCount()
    }

    private fun getAlarms() = viewModelScope.launch(Dispatchers.IO){
        alarmRecordRepository.getAlarms(currentPage)
    }

    fun loadMore() = viewModelScope.launch(Dispatchers.IO){
        currentPage++
        alarmRecordRepository.getAlarms(currentPage)
    }
}