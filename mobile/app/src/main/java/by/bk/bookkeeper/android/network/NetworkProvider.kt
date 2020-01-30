package by.bk.bookkeeper.android.network

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface NetworkProvider {

    /**
     * @param clazz the REST interface definition
     * @param <T> the type of `class`
     * @return an implementation of the `clazz`-defined REST interface with a default timeout backed by the current session
     */
    fun <T> getRestClient(clazz: Class<T>): T

}

class BookkeeperNetworkProvider(private val environment: BookkeeperEnvironment) : NetworkProvider {

    override fun <T> getRestClient(clazz: Class<T>): T =
            ServiceGenerator.createService(clazz, environment.getBaseUrl())

}