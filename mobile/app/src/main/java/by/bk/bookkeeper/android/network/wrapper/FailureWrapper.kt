package by.bk.bookkeeper.android.network.wrapper

import androidx.annotation.StringRes
import by.bk.bookkeeper.android.R
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException


/**
 *  Created by Evgenia Grinkevich on 29, January, 2020
 **/

sealed class FailureWrapper(@StringRes val messageStringRes: Int,
                            val throwable: Throwable? = null,
                            val serverErrorMessage: String? = null) {

    class UnknownHost(messageStringRes: Int, throwable: Throwable?) : FailureWrapper(messageStringRes, throwable)
    class Timeout(messageStringRes: Int, throwable: Throwable?) : FailureWrapper(messageStringRes, throwable)
    class Unauthorized(messageStringRes: Int, throwable: Throwable?, serverErrorMessage: String?) : FailureWrapper(messageStringRes, throwable, serverErrorMessage)
    class BadRequest(messageStringRes: Int, throwable: Throwable?, serverErrorMessage: String?) : FailureWrapper(messageStringRes, throwable, serverErrorMessage)
    class Generic(messageStringRes: Int, throwable: Throwable?, serverErrorMessage: String?) : FailureWrapper(messageStringRes, throwable, serverErrorMessage)
    class Forbidden(messageStringRes: Int, throwable: Throwable?, serverErrorMessage: String?) : FailureWrapper(messageStringRes, throwable, serverErrorMessage)
    class NotFound(messageStringRes: Int, throwable: Throwable?, serverErrorMessage: String?) : FailureWrapper(messageStringRes, throwable, serverErrorMessage)

    companion object {

        fun getFailureType(error: Throwable): FailureWrapper = when (error) {
            is HttpException -> {
                val serverMessage = error.response()?.errorBody()?.string()
                when (error.response()?.code()) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> BadRequest(R.string.err_bad_request, error, serverMessage)
                    HttpURLConnection.HTTP_NOT_FOUND -> NotFound(R.string.err_not_found, error, serverMessage)
                    HttpURLConnection.HTTP_UNAUTHORIZED -> Unauthorized(R.string.err_unauthorized, error, serverMessage)
                    HttpURLConnection.HTTP_CLIENT_TIMEOUT -> Timeout(R.string.err_timeout, error)
                    HttpURLConnection.HTTP_FORBIDDEN -> Forbidden(R.string.err_forbidden, error, serverMessage)
                    else -> Generic(R.string.err_unknown, error, serverMessage)
                }
            }
            is UnknownHostException -> UnknownHost(R.string.err_unknown_host, error)
            is SocketTimeoutException -> Timeout(R.string.err_timeout, error)
            else -> Generic(R.string.err_unknown, error, serverErrorMessage = null)
        }
    }
}