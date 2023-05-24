package by.bk.bookkeeper.android.sms

/**
 *  Created by Evgenia Grinkevich on 30, January, 2020
 **/
data class SMS(
    val body: String,
    val dateReceived: Long,
    val senderName: String
)
