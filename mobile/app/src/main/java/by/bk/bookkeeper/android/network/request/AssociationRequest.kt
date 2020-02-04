package by.bk.bookkeeper.android.network.request

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class AssociationRequest(@SerializedName("account")
                              val accountName: String,
                              @SerializedName("subAccount")
                              val subAccountName: String,
                              @SerializedName("sender")
                              val sender: String,
                              @SerializedName("subAccountIdentifier")
                              val associationString: String? = null
)