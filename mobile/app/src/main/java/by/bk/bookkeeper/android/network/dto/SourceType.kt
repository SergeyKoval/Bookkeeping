package by.bk.bookkeeper.android.network.dto

import android.content.Context
import by.bk.bookkeeper.android.R

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

enum class SourceType(private val key: String) {
    SMS("SMS"),
    PUSH("PUSH"),
    UNKNOWN("UNKNOWN");

    companion object {

        fun mapToSourceType(key: String): SourceType = values().find { it.key == key } ?: UNKNOWN

        fun getUiString(context: Context, sourceType: SourceType) = when (sourceType) {
            SMS -> context.getString(R.string.association_type_sms)
            PUSH -> context.getString(R.string.association_type_push)
            else -> ""
        }
    }

}