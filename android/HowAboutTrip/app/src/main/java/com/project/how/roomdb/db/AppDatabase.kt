package com.project.how.roomdb.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.project.how.data_class.roomdb.Alarm
import com.project.how.data_class.roomdb.RecentAirplane
import com.project.how.roomdb.converters.DateTypeConverter
import com.project.how.roomdb.dao.AlarmDao
import com.project.how.roomdb.dao.RecentAirplaneDao


@Database(entities = [RecentAirplane::class, Alarm::class], version = 3, exportSchema = false)
@TypeConverters(DateTypeConverter::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun recentAirplaneDao(): RecentAirplaneDao
    abstract fun alarmDao(): AlarmDao
}