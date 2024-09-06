package com.project.how.data_class.dto.recode.image

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime

data class ImagesResponse(
    @SerializedName("scheduleImages")
    val scheduleImages : List<ImageElement>
)

data class ImageElement(
    @SerializedName("scheduleImageId")
    val id : Long,
    @SerializedName("path")
    val url : String,
    @SerializedName("saveDate")
    val date : String
)

data class AddedImage(
    @SerializedName("id")
    val id : Long,
    @SerializedName("path")
    val url : String,
    @SerializedName("saveDate")
    val date : String
)
