package by.bk.bookkeeper.android.network.dto

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

data class DeviceMessage(
    @SerializedName("sender")
    val sender: String,
    @SerializedName("fullText")
    val fullText: String,
    @SerializedName("messageTimestamp")
    val timestamp: Long,
    @SerializedName("source")
    val source: SourceType
)
