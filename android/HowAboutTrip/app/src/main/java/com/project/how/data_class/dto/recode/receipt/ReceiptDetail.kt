package com.project.how.data_class.dto.recode.receipt

import com.google.gson.annotations.SerializedName

data class ReceiptDetail(
    @SerializedName("scheduleId")
    var id : Long,
    @SerializedName("storeName")
    var storeName : String?,
    @SerializedName("storeType")
    var storeType: StoreType,
    @SerializedName("purchaseDate")
    var purchaseDate : String,
    @SerializedName("totalPrice")
    var totalPrice : Double,
    @SerializedName("receiptDetails")
    var receiptDetails : List<ReceiptDetailListItem>
)

data class ReceiptDetailListItem(
    @SerializedName("item")
    var title: String,
    @SerializedName("count")
    var count : Long,
    @SerializedName("itemPrice")
    var itemPrice : Double
)
