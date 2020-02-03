package by.bk.bookkeeper.android.sms

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

data class Sender(val id: String,
                  val address: String = "",
                  val addressBookDisplayableName: String? = null
)