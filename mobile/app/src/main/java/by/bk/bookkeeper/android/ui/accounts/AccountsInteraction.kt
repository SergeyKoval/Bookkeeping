import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import io.reactivex.Observable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
interface AccountsInteraction {

    interface Inputs {
        fun retryLoading()
    }

    interface Outputs {
        fun accounts(): Observable<List<Account>>
        fun accountsRequestState(): Observable<DataStatus>
    }
}