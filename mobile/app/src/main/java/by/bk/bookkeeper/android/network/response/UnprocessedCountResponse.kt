package by.bk.bookkeeper.android.network.response

import com.google.gson.JsonDeserializer
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 15, April, 2020
 **/

data class UnprocessedCountResponse(@SerializedName("status")
                                    val status: String? = null,
                                    @SerializedName("result")
                                    val count: Int? = null,
                                    val receivedDateMillis: Long? = null
) {
    companion object {

        fun createJsonDeserializer(): JsonDeserializer<UnprocessedCountResponse> = JsonDeserializer { json, typeOfT, context ->
            val responseObject = json.asJsonObject
            val status = responseObject.get("status").asString
            val count = responseObject.get("result").asInt
            return@JsonDeserializer UnprocessedCountResponse(status, count, System.currentTimeMillis())
        }
    }
}
