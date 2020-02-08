package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class Account(@SerializedName("opened")
                   val opened: Boolean,
                   @SerializedName("order")
                   val order: Int,
                   @SerializedName("subAccounts")
                   var subAccounts: List<SubAccount>,
                   @SerializedName("title")
                   val title: String
)