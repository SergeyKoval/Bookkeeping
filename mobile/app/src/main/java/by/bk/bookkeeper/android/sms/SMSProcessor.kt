package by.bk.bookkeeper.android.sms

import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.sms.preferences.AssociationInfo
import timber.log.Timber

object SMSProcessor {

    fun createFromPdus(pdus: Array<*>?, format: String?): HashMap<String, ReceivedSms> {
        val receivedSms: HashMap<String, ReceivedSms> = hashMapOf()
        pdus?.forEach { pdu ->
            val sms = ReceivedSms.createFromPdu(pdu as ByteArray, format) ?: return receivedSms
            val originatingAddress = sms.originatingAddress
            if (!receivedSms.containsKey(originatingAddress)) {
                receivedSms[originatingAddress] = sms
            } else {
                receivedSms[originatingAddress]?.apply { body += sms.body }
            }
        }
        return receivedSms
    }

    fun mapToAssociations(receivedSms: HashMap<String, ReceivedSms>,
                          associationInfo: List<AssociationInfo>): ArrayList<MatchedSms> {
        val matchedSms: ArrayList<MatchedSms> = arrayListOf()
        receivedSms.forEach { entry ->
            entry.value.run {
                Timber.d("SMS processing from $senderName")
                associationInfo.forEach { association ->
                    association.smsTemplatesList.forEach { template ->
                        if (template.senderName.equals(senderName, ignoreCase = true)) {
                            val sms = SMS(senderName = senderName, body = body, dateReceived = dateReceived)
                            if (!template.bodyTemplate.isNullOrBlank()) {
                                if (body.contains(template.bodyTemplate, ignoreCase = true)) {
                                    matchedSms.add(MatchedSms(association.accountName, association.subAccountName, sms))
                                }
                            } else {
                                matchedSms.add(MatchedSms(association.accountName, association.subAccountName, sms))
                            }
                        }
                    }
                }
            }
        }
        return matchedSms
    }
}