package by.bk.bookkeeper.android.sms.preferences

/**
 *  Created by Evgenia Grinkevich on 08, February, 2020
 **/
data class AssociationInfo(val accountName: String,
                           val subAccountName: String,
                           val smsTemplatesList: List<AssociatedSMSInfo>
)

data class AssociatedSMSInfo(val senderName: String,
                             val bodyTemplate: String?
)