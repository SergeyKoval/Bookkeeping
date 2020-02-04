package by.bk.bookkeeper.android.sms

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 30, January, 2020
 **/
data class SMS(@SerializedName("fullSms")
               val body: String,
               @SerializedName("smsTimestamp")
               val dateReceived: Long
)
