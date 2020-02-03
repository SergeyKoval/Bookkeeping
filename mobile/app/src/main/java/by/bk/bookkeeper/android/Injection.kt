package by.bk.bookkeeper.android

import androidx.lifecycle.ViewModelProvider
import by.bk.bookkeeper.android.network.BookkeeperService
import by.bk.bookkeeper.android.network.BookkeeperNetworkProvider

/**
 *  Created by Evgenia Grinkevich on 30, January, 2020
 **/

object Injection {

    fun provideBookkeeperService() = BookkeeperNetworkProvider(BookkeeperApp.getEnvironment())
            .getRestClient(BookkeeperService::class.java)

    fun provideViewModelFactory(): ViewModelProvider.Factory = ViewModelFactory(provideBookkeeperService())

}