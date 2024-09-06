package com.project.how.data_class.dto.recode.image

import com.google.gson.annotations.SerializedName

data class SaveImageRequest(
    @SerializedName("scheduleId")
    val id : Long,
    @SerializedName("saveDate")
    val date : String
)
