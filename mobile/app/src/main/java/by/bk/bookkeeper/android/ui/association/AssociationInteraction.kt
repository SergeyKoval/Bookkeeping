import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.accounts.AssociationRequestLoadingState
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
interface AssociationInteraction {

    interface Inputs {
        fun addAssociation(associationRequest: AssociationRequest)
        fun reloadConversations()
    }

    interface Outputs {
        fun conversations(): Observable<List<Conversation>>
        fun conversationsLoadingState(): Observable<DataStatus>
        fun associationRequestState(): Observable<AssociationRequestLoadingState>
    }
}