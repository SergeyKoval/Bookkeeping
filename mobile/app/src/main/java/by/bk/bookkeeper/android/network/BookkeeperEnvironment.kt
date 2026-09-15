package by.bk.bookkeeper.android.network

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/
enum class BookkeeperEnvironment {

    PROD {
        override fun getBaseUrl(): String = "https://bookkeeper.deplake.by"
    };

    abstract fun getBaseUrl(): String
}