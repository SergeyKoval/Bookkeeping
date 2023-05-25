package by.bk.bookkeeper.android

import androidx.lifecycle.ViewModelProvider
import by.bk.bookkeeper.android.network.BookkeeperNetworkProvider
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.push.PushMessage
import by.bk.bookkeeper.processor.MessageProcessor
import by.bk.bookkeeper.processor.MessagesRepository
import by.bk.bookkeeper.processor.PushProcessor
import by.bk.bookkeeper.processor.SMSProcessor

/**
 *  Created by Evgenia Grinkevich on 30, January, 2020
 **/

object Injection {

    fun provideBookkeeperService() = BookkeeperNetworkProvider(BookkeeperApp.getEnvironment())
        .getRestClient(BookkeeperService::class.java)

    fun provideViewModelFactory(): ViewModelProvider.Factory = ViewModelFactory(provideBookkeeperService())

    fun provideMessagesRepository() = MessagesRepository(provideBookkeeperService())

    fun provideSmsProcessor(): MessageProcessor<Any> = SMSProcessor()

    fun providePushProcessor(): MessageProcessor<PushMessage> = PushProcessor()

}