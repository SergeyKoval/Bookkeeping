import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.network.response.UnprocessedCountResponse
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
interface PendingMessagesInteraction {

    interface Inputs {
        fun sendMessagesToServer(type: SourceType)
        fun getServerUnprocessedCount()
    }

    interface Outputs {
        fun pendingMessages(): Observable<List<ProcessedMessage>>
        fun pendingMessagesMap(): Observable<Map<SourceType, List<ProcessedMessage>>>
        fun serverUnprocessedCount(): Observable<UnprocessedCountResponse>
        fun messagesLoadingState(): Observable<DataStatus>
    }
}