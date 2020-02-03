package by.bk.bookkeeper.android.network

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/
enum class BookkeeperEnvironment {

    PROD {
        override fun getBaseUrl(): String = "https://deplake.tk"
    };

    abstract fun getBaseUrl(): String
}