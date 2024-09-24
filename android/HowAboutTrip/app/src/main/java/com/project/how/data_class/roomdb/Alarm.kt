package com.project.how.data_class.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "alarm_table")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text : String,
    var isChecked : Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Date = Date()
)
