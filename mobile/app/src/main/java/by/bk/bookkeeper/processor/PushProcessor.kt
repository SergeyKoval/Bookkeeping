package by.bk.bookkeeper.processor

import by.bk.bookkeeper.android.network.dto.DeviceMessage
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.push.PushMessage
import by.bk.bookkeeper.android.sms.preferences.AssociationInfo
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

class PushProcessor : MessageProcessor<PushMessage> {

    override fun process(messages: List<PushMessage>?, rule: String?): List<ProcessedMessage> {
        val associationInfo: List<AssociationInfo> = SharedPreferencesProvider.getAssociationsFromStorage()
        val matchedPushes: ArrayList<ProcessedMessage> = arrayListOf()
        messages?.forEach { push ->
            val packageName = push.packageName
            val text = push.text
            val postTime = push.timestamp
            associationInfo.forEach { info ->
                info.associationList.forEach { association ->
                    if (association.senderName.equals(packageName, ignoreCase = true)) {
                        val processedMessage = ProcessedMessage(
                            account = info.accountName,
                            subAccount = info.subAccountName,
                            deviceMessage = DeviceMessage(
                                sender = packageName,
                                fullText = text,
                                timestamp = postTime,
                                source = SourceType.PUSH
                            )
                        )
                        if (association.bodyTemplate.isNullOrEmpty()) {
                            matchedPushes.add(processedMessage)
                        } else {
                            if (text.contains(association.bodyTemplate, ignoreCase = true)) {
                                matchedPushes.add(processedMessage)
                            }
                        }
                    }
                }
            }
        }
        return matchedPushes
    }
}