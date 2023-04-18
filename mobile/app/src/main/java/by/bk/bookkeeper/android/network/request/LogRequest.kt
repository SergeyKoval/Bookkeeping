package by.bk.bookkeeper.android.network.request

import com.google.gson.annotations.SerializedName

data class LogRequest(
    @SerializedName("message")
    val message: String
)