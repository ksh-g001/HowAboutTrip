package com.project.how.data_class

import java.io.Serializable


data class ScheduleSimpleInfo(
    val id : Long,
    val name : String,
    val startDate : String,
    val endDate : String
): Serializable
