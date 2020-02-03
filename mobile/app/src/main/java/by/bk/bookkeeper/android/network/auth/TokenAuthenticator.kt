package by.bk.bookkeeper.android.network.auth

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

class TokenAuthenticator : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            SessionDataProvider.clearSessionData()
        }
        return null
    }
}