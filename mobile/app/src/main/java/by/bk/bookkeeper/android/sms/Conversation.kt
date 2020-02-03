package by.bk.bookkeeper.android.sms

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

data class Conversation(val threadId: Long = 0,
                        val sender: Sender? = null
)