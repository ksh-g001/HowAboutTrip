package com.project.how.data_class.dto

import com.google.gson.annotations.SerializedName

data class GetCountryLocationRequest(
    @SerializedName("country") val country : String
)
