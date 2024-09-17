package com.project.how.view_model

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.how.data_class.Setting
import com.project.how.model.SettingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class SettingViewModel : ViewModel() {
    private val settingRepository = SettingRepository()
    private var _settingLiveData = settingRepository.settingLiveData
    val settingLiveData: LiveData<Setting>
        get() = _settingLiveData
    private var initNeed = true


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun init(context : Context) = viewModelScope.launch(Dispatchers.IO) {
        if (initNeed){
            settingRepository.init(context)
            initNeed = false
        }
    }

    fun setAlarmOn(context : Context) = viewModelScope.launch(Dispatchers.IO) {
        settingRepository.setAlarmOn(context)
    }

    fun setAlarmOff(context : Context) = viewModelScope.launch(Dispatchers.IO) {
        settingRepository.setAlarmOff(context)
    }

    fun setLocationOn(context : Context) = viewModelScope.launch(Dispatchers.IO) {
        settingRepository.setLocationOn(context)
    }

    fun setLocationOff(context : Context) = viewModelScope.launch(Dispatchers.IO) {
        settingRepository.setLocationOff(context)
    }
}