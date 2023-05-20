package by.bk.bookkeeper.processor

import by.bk.bookkeeper.android.network.request.ProcessedMessage

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

interface MessageProcessor<T> {

    fun process(messages: List<T>?, rule: String? = null): List<ProcessedMessage>
}
