package by.bk.bookkeeper.android.network.response

import by.bk.bookkeeper.android.network.auth.SessionDataProvider
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class SubAccount(@SerializedName("device")
                      val associations: List<Association> = listOf(),
                      @SerializedName("icon")
                      val icon: String,
                      @SerializedName("order")
                      val order: Int,
                      @SerializedName("title")
                      val title: String
) {

    companion object {

        fun createJsonDeserializer(): JsonDeserializer<SubAccount> = JsonDeserializer { json, typeOfT, context ->
            val deviceAssociationsMap = (json as JsonObject).get("device")?.asJsonObject?.entrySet()
            val associations: ArrayList<Association> = arrayListOf()
            deviceAssociationsMap?.let { map ->
                for (entry in map) {
                    if (entry.key == SessionDataProvider.getDeviceId()) {
                        val jsonAssociations = entry.value as JsonArray
                        jsonAssociations.forEach {
                            val element = it.asJsonObject
                            associations.add(Association(sender = element.get("sender").asString,
                                    smsBodyTemplate = element.get("subAccountIdentifier").asString))
                        }
                    }
                }
            }
            return@JsonDeserializer SubAccount(
                    associations = associations,
                    icon = json.get("icon").asString,
                    order = json.get("order").asInt,
                    title = json.get("title").asString
            )
        }
    }

}