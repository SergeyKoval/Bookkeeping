package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class BaseResponse(@SerializedName("message")
                        val message: String? = null,
                        @SerializedName("status")
                        val status: String? = null
)
