import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.ui.accounts.AssociationRequestLoadingState
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
interface AssociationInteraction {

    interface Inputs {
        fun addAssociation(associationRequest: AssociationRequest)
    }

    interface Outputs {
        fun sms(): Observable<Map<String, List<SMS>>>
        fun storedSmsLoadingState(): Observable<DataStatus>
        fun associationRequestState(): Observable<AssociationRequestLoadingState>
    }
}