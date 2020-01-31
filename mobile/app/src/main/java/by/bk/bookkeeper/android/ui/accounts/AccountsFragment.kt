package by.bk.bookkeeper.android.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_accounts.*

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountsFragment : BaseFragment() {

    private val accountsViewModel: AccountsViewModel by activityScopeViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_accounts.layoutManager = LinearLayoutManager(context)
        account_swipe_refresh.setOnRefreshListener {
            accountsViewModel.retryLoading()
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                accountsViewModel.accounts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            recycler_accounts.adapter = AccountsAdapter(it)
                        },
                accountsViewModel.accountsRequestState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            account_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                            if (dataStatus is DataStatus.Error) showErrorSnackbar(dataStatus.failure)
                            if (dataStatus is DataStatus.Empty) {
                                Toast.makeText(context, getString(R.string.msg_no_accounts), Toast.LENGTH_LONG).show()
                            }
                        })
    }

    override fun retryLoading() = accountsViewModel.retryLoading()

    override fun getTAG() = TAG

    companion object {

        val TAG = AccountsFragment::class.java.simpleName

        fun newInstance() = AccountsFragment()
    }
}
