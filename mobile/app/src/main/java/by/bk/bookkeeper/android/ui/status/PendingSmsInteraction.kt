import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
interface PendingSmsInteraction {

    interface Inputs {
        fun sendSmsToServer()
    }

    interface Outputs {
        fun pendingSms(): Observable<List<MatchedSms>>
        fun smsLoadingState(): Observable<DataStatus>
    }
}