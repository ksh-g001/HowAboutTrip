package com.project.how.data_class.recyclerview.schedule

import java.io.Serializable

data class AiScheduleList(
    val aiSchedules : ArrayList<AiSchedule>
) : Serializable

data class AiSchedule(
    val title: String,
    val country: String,
    val currency : String,
    val budget : Double,
    val places: List<String>,
    val image: String,
    val startDate: String,
    val endDate: String,
    val dailySchedule: List<List<AiDaysSchedule>>
) : Serializable

data class AiDaysSchedule(
    val type : Int,
    val todo: String,
    val budget : Double,
    val places: String,
    val lat : Double,
    val lng : Double
) : Serializable
