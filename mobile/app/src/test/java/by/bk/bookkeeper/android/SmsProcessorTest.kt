package by.bk.bookkeeper.android

import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.sms.ReceivedSms
import by.bk.bookkeeper.android.sms.SMSProcessor
import by.bk.bookkeeper.android.sms.preferences.AssociatedSMSInfo
import by.bk.bookkeeper.android.sms.preferences.AssociationInfo
import org.junit.Test

class SmsProcessorTest {

    private val smsTemplateCode: String = "Конфиденциально! Код для подтверждения транзакции xJ+vcj85e4n <#> 221245"
    private val smsTemplateTransactionInfo: String = "Card#2905; Oplata v internete: 1.00 BYN; 25.01.2022 17:20:14; BNB PAY 4>MINSK>BY; Dostupno 000.97 BYN"

    private val associationInfo: AssociationInfo = AssociationInfo(
            accountName = "test",
            subAccountName = "",
            smsTemplatesList = listOf(
                    AssociatedSMSInfo("BNB_BANK", ""),
                    AssociatedSMSInfo("BNB-Bank", ""),
                    AssociatedSMSInfo("BNB-BANK", "Card#2905"),
            )
    )

    private val receivedSms: HashMap<String, ReceivedSms> = hashMapOf(
            "BNB_BANK" to ReceivedSms(
                    originatingAddress = "BNB_BANK",
                    senderName = "BNB_BANK",
                    body = smsTemplateCode,
                    dateReceived = 0),
            "BNB-Bank" to ReceivedSms(
                    originatingAddress = "BNB-Bank",
                    senderName = "BNB-Bank",
                    body = "123545",
                    dateReceived = 0),
            "BNB-BANK" to ReceivedSms(
                    originatingAddress = "BNB-BANK",
                    senderName = "BNB-BANK",
                    body = smsTemplateTransactionInfo,
                    dateReceived = 0)
    )

    @Test
    fun association_mapping_correct() {
        val mappedSms: ArrayList<MatchedSms> = SMSProcessor.mapToAssociations(receivedSms, listOf(associationInfo))
        assert(!mappedSms.isNullOrEmpty())
    }

}