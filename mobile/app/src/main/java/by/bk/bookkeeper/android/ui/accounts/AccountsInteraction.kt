import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.request.DissociationRequest
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.accounts.AssociationRequestLoadingState
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
interface AccountsInteraction {

    interface Inputs {
        fun refreshAccounts()
        fun removeAssociation(dissociationRequest: DissociationRequest)
        fun addAssociation(associationRequest: AssociationRequest)
    }

    interface Outputs {
        fun accounts(): Observable<List<Account>>
        fun accountsRequestState(): Observable<DataStatus>
        fun associationRequestState(): Observable<AssociationRequestLoadingState>
    }
}