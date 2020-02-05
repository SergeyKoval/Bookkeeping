package by.bk.bookkeeper.android.network.response

import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class Association(@SerializedName("sender")
                       val sender: String,
                       @SerializedName("subAccountIdentifier")
                       val smsBodyTemplate: String?
)