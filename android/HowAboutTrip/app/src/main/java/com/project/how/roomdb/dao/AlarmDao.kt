package com.project.how.roomdb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.how.data_class.roomdb.Alarm

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm_table")
    suspend fun getAllAlarms(): List<Alarm>

    @Query("SELECT * FROM alarm_table LIMIT :pageSize OFFSET :pageIndex")
    suspend fun getItems(pageSize: Int, pageIndex: Int): List<Alarm>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data : Alarm) : Long

    @Query("UPDATE alarm_table SET isChecked = :isChecked WHERE id = :id")
    suspend fun check(id : Int, isChecked : Boolean)

    @Query("DELETE FROM alarm_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM alarm_table WHERE isChecked = 0")
    suspend fun hasUncheckedItems(): Int

    @Query("SELECT COUNT(*) FROM alarm_table")
    suspend fun getAllCount() : Int
}