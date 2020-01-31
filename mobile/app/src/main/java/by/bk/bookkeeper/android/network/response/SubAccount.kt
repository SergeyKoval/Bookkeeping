package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class SubAccount(@SerializedName("device")
                      val association: Association,
                      @SerializedName("icon")
                      val icon: String,
                      @SerializedName("order")
                      val order: Int,
                      @SerializedName("title")
                      val title: String
)