package by.bk.bookkeeper.android.network.response

import by.bk.bookkeeper.android.network.dto.SourceType
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class Association(
    @SerializedName("sender")
    val sender: String,
    @SerializedName("subAccountIdentifier")
    val smsBodyTemplate: String?,
    @SerializedName("source")
    val sourceType: SourceType
)