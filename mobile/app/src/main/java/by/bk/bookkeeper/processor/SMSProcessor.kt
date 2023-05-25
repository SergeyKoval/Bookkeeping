package by.bk.bookkeeper.processor

import by.bk.bookkeeper.android.network.dto.DeviceMessage
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.sms.preferences.AssociationInfo
import by.bk.bookkeeper.android.sms.preferences.SharedPreferencesProvider
import timber.log.Timber

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/

class SMSProcessor : MessageProcessor<Any> {

    override fun process(messages: List<Any>?, rule: String?): List<ProcessedMessage> {
        val associationInfo: List<AssociationInfo> = SharedPreferencesProvider.getAssociationsFromStorage()
        val matchedSms: ArrayList<ProcessedMessage> = arrayListOf()
        convertPdus(messages, rule).forEach { smsEntry ->
            smsEntry.value.run {
                Timber.d("SMS processing from $senderName")
                associationInfo.forEach { info ->
                    info.associationList.forEach { association ->
                        if (association.senderName.equals(senderName, ignoreCase = true)) {
                            val processedSms = ProcessedMessage(
                                account = info.accountName,
                                subAccount = info.subAccountName,
                                deviceMessage = DeviceMessage(
                                    sender = senderName,
                                    fullText = body,
                                    timestamp = dateReceived,
                                    source = SourceType.SMS
                                )
                            )
                            if (association.bodyTemplate.isNullOrEmpty()) {
                                matchedSms.add(processedSms)
                            } else {
                                if (body.contains(association.bodyTemplate, ignoreCase = true)) {
                                    matchedSms.add(processedSms)
                                }
                            }
                        }
                    }
                }
            }
        }
        return matchedSms
    }

    private fun convertPdus(messages: List<Any>?, rule: String?): HashMap<String, ReceivedSms> {
        val receivedSms: HashMap<String, ReceivedSms> = hashMapOf()
        messages?.forEach { pdu ->
            val sms = ReceivedSms.createFromPdu(pdu as ByteArray, rule) ?: return receivedSms
            val originatingAddress = sms.originatingAddress
            if (!receivedSms.containsKey(originatingAddress)) {
                receivedSms[originatingAddress] = sms
            } else {
                receivedSms[originatingAddress]?.apply { body += sms.body }
            }
        }
        return receivedSms
    }

}