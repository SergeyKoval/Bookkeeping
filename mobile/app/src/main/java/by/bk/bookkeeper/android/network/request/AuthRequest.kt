package by.bk.bookkeeper.android.network.request

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

data class AuthRequest(@SerializedName("deviceId")
                       val deviceId: String,
                       @SerializedName("email")
                       val email: String,
                       @SerializedName("password")
                       val password: String
)