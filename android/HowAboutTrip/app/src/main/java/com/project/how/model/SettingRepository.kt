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
import javax.inject.Singleton


@Singleton
class SettingRepository {
    private var alarmOn = false
    private var locationOn = false
    private var _settingLiveData = MutableLiveData<Setting>()
    val settingLiveData: LiveData<Setting>
        get() = _settingLiveData

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun init(context : Context){
        val alarmScope = CoroutineScope(Dispatchers.IO).launch {
            SettingDataStore.getAlarmStatus(context).collect{
                alarmOn = it
                Log.d("SettingRepository init", "alarmOn : $alarmOn")
            }
        }

        val locationScope = CoroutineScope(Dispatchers.IO).launch {
            SettingDataStore.getLocationStatus(context).collect{
                locationOn = it
                Log.d("SettingRepository init", "locationOn : $locationOn")
            }
        }

        joinAll(alarmScope, locationScope)
        Log.d("SettingRepository init", "alarmOn : $alarmOn, locationOn : $locationOn")
        _settingLiveData.postValue(Setting(alarmOn, locationOn))
    }

    fun setAlarmOn(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setAlarmOn(context)
        alarmOn = true
        _settingLiveData.postValue(Setting(alarmOn, locationOn))

    }

    fun setAlarmOff(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setAlarmOff(context)
        alarmOn = false
        _settingLiveData.postValue(Setting(alarmOn, locationOn))

    }

    fun setLocationOn(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setLocationOn(context)
        locationOn = true
        _settingLiveData.postValue(Setting(alarmOn, locationOn))
    }

    fun setLocationOff(context : Context) = CoroutineScope(Dispatchers.IO).launch {
        SettingDataStore.setLocationOff(context)
        locationOn = false
        _settingLiveData.postValue(Setting(alarmOn, locationOn))
    }
}