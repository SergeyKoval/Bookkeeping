package by.bk.bookkeeper.android.network.auth

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

class TokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val currentToken: String? = SessionDataProvider.getCurrentSessionData()?.token
        val originalRequest = chain.request()
        Timber.d("Token intercept invoked, token: $currentToken")
        return if (currentToken == null) {
            chain.proceed(originalRequest)
        } else {
            chain.proceed(
                    originalRequest.newBuilder()
                            .addHeader("Authorization", "Bearer $currentToken")
                            .addHeader("Accept", "application/json")
                            .build()
            )
        }
    }
}
