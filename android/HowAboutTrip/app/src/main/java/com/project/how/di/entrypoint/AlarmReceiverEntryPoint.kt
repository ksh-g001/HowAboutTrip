package com.project.how.di.entrypoint

import com.project.how.roomdb.dao.AlarmDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AlarmReceiverEntryPoint {
    fun alarmDao(): AlarmDao
}