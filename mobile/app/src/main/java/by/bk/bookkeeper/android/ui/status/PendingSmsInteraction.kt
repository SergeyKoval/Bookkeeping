import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
interface PendingSmsInteraction {

    interface Inputs {
        fun sendSmsToServer()
        fun getServerUnprocessedCount()
    }

    interface Outputs {
        fun pendingSms(): Observable<List<MatchedSms>>
        fun serverUnprocessedCount(): Observable<UnprocessedCountResponse>
        fun smsLoadingState(): Observable<DataStatus>
    }
}