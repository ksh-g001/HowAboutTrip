package com.project.how.data_class.dto

import com.google.gson.annotations.SerializedName

data class GenerateSkyscannerUrlResponse(
    @SerializedName("skyscannerUrl")
    val url : String
)
