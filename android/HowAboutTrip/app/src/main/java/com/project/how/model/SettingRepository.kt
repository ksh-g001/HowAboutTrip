package com.project.how.model

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project.how.data_class.Setting
import com.project.how.data_store.SettingDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton


@Singleton
class SettingRepository {
    private val setting = Setting(false)
    private var _settingLiveData = MutableLiveData<Setting>()
    val settingLiveData: LiveData<Setting>
        get() = _settingLiveData

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun init(context : Context){
        val alarmScope = CoroutineScope(Dispatchers.IO).launch {
            SettingDataStore.getAlarmStatus(context).collect {
                setting.alarmSettingStatus = it
                Log.d("SettingRepository init", "alarmOn : $it")
            }
        }

        alarmScope.join()

        Log.d("SettingRepository init", "end $setting")
        _settingLiveData.postValue(setting)
    }

    fun setAlarmOn(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setAlarmOn(context)
        setting.alarmSettingStatus = true
        _settingLiveData.postValue(setting)

    }

    fun setAlarmOff(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setAlarmOff(context)
        setting.alarmSettingStatus = false
        _settingLiveData.postValue(setting)

    }
}