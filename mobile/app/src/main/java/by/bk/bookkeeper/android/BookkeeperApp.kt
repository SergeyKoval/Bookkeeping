package by.bk.bookkeeper.android

import android.app.Application
import android.content.Context
import by.bk.bookkeeper.android.network.BookkeeperEnvironment
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class BookkeeperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }

    companion object {
        private lateinit var instance: BookkeeperApp
        fun getContext(): Context = instance.applicationContext
        fun getEnvironment(): BookkeeperEnvironment = BookkeeperEnvironment.PROD
    }

}