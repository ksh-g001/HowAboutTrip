package com.project.how.data_class.dto.recode.image

import com.google.gson.annotations.SerializedName

data class LocationSchedules(
    @SerializedName("locationSchedules") val locationSchedules : List<LocationSchedulesItem>
)

data class LocationSchedulesItem(
    @SerializedName("latitude") val lat : Double,
    @SerializedName("longitude") val lng : Double,
    @SerializedName("scheduleIds") val ids : List<Long>
)
