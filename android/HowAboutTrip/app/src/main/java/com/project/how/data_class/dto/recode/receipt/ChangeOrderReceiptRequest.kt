package com.project.how.data_class.dto.recode.receipt

import com.google.gson.annotations.SerializedName

data class ChangeOrderReceiptRequest(
    @SerializedName("receiptId")
    val id : Long,
    @SerializedName("purchaseDate")
    val purchaseDate : String,
    @SerializedName("orderNum")
    val orderNum : Long
)
