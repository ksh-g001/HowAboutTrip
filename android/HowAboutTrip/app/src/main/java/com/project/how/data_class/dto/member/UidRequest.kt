package com.project.how.data_class.dto.member
import com.google.gson.annotations.SerializedName

data class UidRequest (
    @SerializedName("uid") val uid : String
)