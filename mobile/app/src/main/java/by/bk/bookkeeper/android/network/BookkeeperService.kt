package by.bk.bookkeeper.android.network

import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.request.AuthRequest
import by.bk.bookkeeper.android.network.request.DissociationRequest
import by.bk.bookkeeper.android.network.request.LogRequest
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.AuthResponse
import by.bk.bookkeeper.android.network.response.BaseResponse
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

interface BookkeeperService {

    @POST("/token/generate-token-mobile")
    fun login(@Body authRequest: AuthRequest): Single<AuthResponse>

    @GET("/api/profile/accounts")
    fun getAccounts(): Single<List<Account>>

    @GET("/api/history/unprocessed-count")
    fun getUnprocessedSmsMessagesSingle(): Single<UnprocessedCountResponse>

    @GET("/api/history/unprocessed-count")
    fun getUnprocessedMessagesCount(): Call<UnprocessedCountResponse>

    @POST("/api/profile/assign-sub-account")
    fun associateWithAccount(@Body associationRequest: AssociationRequest): Single<BaseResponse>

    @POST("/api/profile/deassign-sub-account")
    fun dissociateFromAccount(@Body dissociationRequest: DissociationRequest): Single<BaseResponse>

    @POST("/api/history/push")
    fun sendPushesObservable(@Body pushes: List<ProcessedMessage>): Single<BaseResponse>

    @POST("/api/history/sms")
    fun sendSmsObservable(@Body sms: List<ProcessedMessage>): Single<BaseResponse>

    @POST("/api/history/push")
    fun sendPushes(@Body pushes: List<ProcessedMessage>): Call<BaseResponse>

    @POST("/api/history/sms")
    fun sendSms(@Body sms: List<ProcessedMessage>): Call<BaseResponse>

    @POST("/logs")
    fun sendLog(@Body logRequest: LogRequest): Single<BaseResponse>
}
