package by.bk.bookkeeper.processor

import android.telephony.SmsMessage

data class ReceivedSms(
    val originatingAddress: String,
    val senderName: String,
    var body: String,
    val dateReceived: Long
) {

    companion object {

        fun createFromPdu(pdu: ByteArray?, format: String?): ReceivedSms? {
            val receivedSMS: SmsMessage = SmsMessage.createFromPdu(pdu, format) ?: return null
            val originatingAddress = receivedSMS.originatingAddress ?: ""
            val sender = receivedSMS.displayOriginatingAddress
            val message = receivedSMS.displayMessageBody
            val dateReceived = receivedSMS.timestampMillis
            return ReceivedSms(originatingAddress = originatingAddress, senderName = sender, body = message, dateReceived = dateReceived)
        }
    }
}