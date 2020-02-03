package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

data class ErrorResponse(@SerializedName("error")
                        val error: String?,

                         @SerializedName("message")
                        val message: String?,

                         @SerializedName("path")
                        val path: String?,

                         @SerializedName("status")
                        val status: Int?,

                         @SerializedName("timestamp")
                        val timestamp: String?
)