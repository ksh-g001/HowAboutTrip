package com.project.how.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.project.how.data_class.roomdb.Alarm
import com.project.how.roomdb.dao.AlarmDao
import com.project.how.roomdb.dao.RecentAirplaneDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

class AlarmRecordRepository @Inject constructor(
    private val recentAirplaneDao: RecentAirplaneDao,
    private val alarmDao: AlarmDao
) {
    private val pageSize = 20
    private var _countLiveData = MutableLiveData<Int>()
    private var maxPage = 0
    private val _alarmsLiveData = MutableLiveData<List<Alarm>>()
    private val _lastLiveData = MutableLiveData<Boolean>()
    val alarmsLiveData: MutableLiveData<List<Alarm>>
        get() = _alarmsLiveData
    val lastLiveData: MutableLiveData<Boolean>
        get() = _lastLiveData
    val countLiveData: MutableLiveData<Int>
        get() = _countLiveData

    init {
        countLiveData.observeForever {
            maxPage = it / pageSize
        }
    }

    suspend fun getCount(){
        _countLiveData.postValue(alarmDao.getAllCount())
    }

    suspend fun getAllAlarms(){
        _lastLiveData.postValue(true)
        _alarmsLiveData.postValue(alarmDao.getAllAlarms())
    }

    suspend fun getAlarms(currentPage : Int = 0) {
        Log.d("AlarmRecordRepository", "count: ${countLiveData.value}\ncurrentPage: $currentPage\nmaxPage: $maxPage")
        if (currentPage+1 < maxPage){
            val alarms = alarmDao.getItems(pageSize, currentPage)
            Log.d("AlarmRecordRepository", "alarms: $alarms")
            _alarmsLiveData.postValue(alarms)
            return
        } else if (currentPage+1 == maxPage || maxPage == 0){
            _lastLiveData.postValue(true)
            _alarmsLiveData.postValue(alarmDao.getItems(pageSize, currentPage))
        }
    }

    suspend fun addAlarm(alarm: Alarm) {
        alarmDao.insert(alarm)
    }

    suspend fun check(id : Int){
        alarmDao.check(id, true)
    }

    suspend fun deleteAll(){
        alarmDao.deleteAll()
    }


}