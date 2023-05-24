package by.bk.bookkeeper.android.network.request

import by.bk.bookkeeper.android.network.dto.SourceType
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
data class DissociationRequest(
    @SerializedName("account")
    val accountName: String,
    @SerializedName("subAccount")
    val subAccountName: String,
    @SerializedName("sender")
    val associationSender: String,
    @SerializedName("subAccountIdentifier")
    val associationTemplate: String? = null,
    @SerializedName("source")
    val source: SourceType
)