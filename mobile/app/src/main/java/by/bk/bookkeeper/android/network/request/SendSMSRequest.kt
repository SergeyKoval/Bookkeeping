package by.bk.bookkeeper.android.network.request

import by.bk.bookkeeper.android.sms.SMS

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class SendSMSRequest(val sms: List<SMS>)