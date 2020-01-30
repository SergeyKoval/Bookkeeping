package by.bk.bookkeeper.android.network

import by.bk.bookkeeper.android.network.request.AuthRequest
import by.bk.bookkeeper.android.network.response.AuthResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface BookkeeperService {

    @POST("/token/generate-token-mobile")
    fun login(@Body authRequest: AuthRequest): Single<AuthResponse>

}
