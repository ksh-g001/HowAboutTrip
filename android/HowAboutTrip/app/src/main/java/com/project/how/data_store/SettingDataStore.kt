package com.project.how.data_store

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


object SettingDataStore {
    private val Context.dataStore by preferencesDataStore("setting")

    private val ALARM_ON = booleanPreferencesKey("alarm_on")
    private val LOCATION_ON = booleanPreferencesKey("location_on")

    suspend fun setAlarmOn(context: Context){
        context.dataStore.edit {
            it[ALARM_ON] = true
        }
    }
    suspend fun setAlarmOff(context: Context){
        context.dataStore.edit {
            it[ALARM_ON] = false
        }
    }

    suspend fun setLocationOn(context: Context){
        context.dataStore.edit {
            it[LOCATION_ON] = true
        }
    }
    suspend fun setLocationOff(context: Context){
        context.dataStore.edit {
            it[LOCATION_ON] = false
        }
    }

    private fun setAlarmStatus(context: Context, alarmOnStatus: Boolean)= CoroutineScope(Dispatchers.IO).launch {
        context.dataStore.edit {
            it[ALARM_ON] = alarmOnStatus
        }
    }

    private fun setLocationStatus(context: Context, locationOnStatus: Boolean)= CoroutineScope(Dispatchers.IO).launch {
        context.dataStore.edit {
            it[LOCATION_ON] = locationOnStatus
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getAlarmStatus(context: Context) : Flow<Boolean> = flow{
        context.dataStore.data.collect{
            val alarmOn = it[ALARM_ON]
            if(alarmOn == null){
                val notifyPermission = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                setAlarmStatus(context, notifyPermission)
                this.emit(notifyPermission)
            }else{
                this.emit(alarmOn)
            }

        }
    }

    fun getLocationStatus(context: Context) : Flow<Boolean> = flow{
        context.dataStore.data.collect {
            val locationOn = it[LOCATION_ON]
            if (locationOn == null) {
                val notifyPermission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).all {
                    context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                }
                setLocationStatus(context, notifyPermission)
                this.emit(notifyPermission)
            } else {
                this.emit(locationOn)
            }
        }
    }
}