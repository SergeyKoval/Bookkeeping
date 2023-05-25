package by.bk.bookkeeper.android.network.request

import by.bk.bookkeeper.android.network.dto.DeviceMessage
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

data class ProcessedMessage(
    @SerializedName("account")
    val account: String,
    @SerializedName("subAccount")
    val subAccount: String,
    @SerializedName("deviceMessage")
    val deviceMessage: DeviceMessage

)

