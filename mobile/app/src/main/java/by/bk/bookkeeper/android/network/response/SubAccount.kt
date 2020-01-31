package by.bk.bookkeeper.android.network.response

import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/

data class SubAccount(@SerializedName("device")
                      val association: Association?,
                      @SerializedName("icon")
                      val icon: String,
                      @SerializedName("order")
                      val order: Int,
                      @SerializedName("title")
                      val title: String
) {

    companion object {

        fun createJsonDeserializer(): JsonDeserializer<SubAccount> = JsonDeserializer { json, typeOfT, context ->
            val deviceAssociationsMap = (json as JsonObject).get("device").asJsonObject.entrySet()
            var association: Association? = null
            for (entry in deviceAssociationsMap) {
                //TODO change to device id, test mock
                if (entry.key == "aa-bb-cc") {
                    val jsonAssociation = entry.value as JsonObject
                    association = Association(sender = jsonAssociation.get("sender").asString,
                            smsBodyTemplate = jsonAssociation.get("subAccountIdentifier").asString)
                }
            }
            return@JsonDeserializer SubAccount(association = association, icon = json.get("icon").asString,
                    order = json.get("order").asInt, title = json.get("title").asString)
        }
    }

}