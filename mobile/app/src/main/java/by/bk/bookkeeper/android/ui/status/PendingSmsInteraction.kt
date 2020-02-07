import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.sms.SMS
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
interface PendingSmsInteraction {

    interface Inputs {
        fun sendSmsToServer()
    }

    interface Outputs {
        fun pendingSms(): Observable<List<SMS>>
        fun smsLoadingState(): Observable<DataStatus>
    }
}