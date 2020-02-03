package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

data class AuthResponse(@SerializedName("token")
                        val token: String
)