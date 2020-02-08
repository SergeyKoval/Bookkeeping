package by.bk.bookkeeper.android.network.request

import by.bk.bookkeeper.android.sms.SMS
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 08, February, 2020
 **/
data class MatchedSms(@SerializedName("account")
                      val accountName: String,
                      @SerializedName("subAccount")
                      val subAccountName: String,
                      @SerializedName("sms")
                      val sms: SMS
)