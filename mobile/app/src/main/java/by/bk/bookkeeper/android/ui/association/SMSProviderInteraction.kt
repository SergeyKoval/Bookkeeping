import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.sms.SMS
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
interface SMSProviderInteraction {

    interface Inputs {
        fun reloadConversations()
    }

    interface Outputs {
        fun conversations(): Observable<List<Conversation>>
        fun sms(): Observable<List<SMS>>
        fun conversationsLoadingState(): Observable<DataStatus>
        fun smsLoadingState(): Observable<DataStatus>
    }
}