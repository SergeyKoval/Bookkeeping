package by.bk.bookkeeper.android.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.network.request.DissociationRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.association.AccountInfoHolder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_accounting.view.*
import kotlinx.android.synthetic.main.fragment_accounts.*


/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountsFragment : BaseFragment() {

    private val accountsViewModel: AccountsViewModel by activityScopeViewModel()
    private val accountAdapter: AccountsAdapter by lazy { AccountsAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.rootView?.toolbar?.setNavigationIcon(R.drawable.ic_nav_menu)
        recycler_accounts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = accountAdapter
        }
        account_swipe_refresh.setOnRefreshListener {
            accountsViewModel.refreshAccounts()
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                accountsViewModel.accounts()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { accountAdapter.setData(it) },
                accountsViewModel.accountsRequestState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            account_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                            if (dataStatus is DataStatus.Error) showErrorSnackbar(dataStatus.failure)
                            if (dataStatus is DataStatus.Empty) {
                                Toast.makeText(context, getString(R.string.msg_no_accounts), Toast.LENGTH_LONG).show()
                            }
                        },
                accountsViewModel.associationRequestState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { associationState ->
                            account_swipe_refresh.isRefreshing = associationState.dataStatus is DataStatus.Loading
                            if (associationState.dataStatus is DataStatus.Success) {
                                accountsViewModel.refreshAccounts()
                                Toast.makeText(context, getString(R.string.msg_operation_successful), Toast.LENGTH_SHORT).show()
                            }
                            if (associationState.dataStatus is DataStatus.Error) {
                                Toast.makeText(context, associationState.dataStatus.failure.messageStringRes, Toast.LENGTH_SHORT).show()
                            }
                        },
                accountAdapter.subAccountItemClick()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { clickInfo ->
                            when (clickInfo) {
                                is SubAccountRecyclerClick.RemoveAssociation -> {
                                    accountsViewModel.removeAssociation(DissociationRequest(
                                            accountName = clickInfo.account.title,
                                            subAccountName = clickInfo.subAccount.title))
                                }
                                is SubAccountRecyclerClick.AddAssociation,
                                is SubAccountRecyclerClick.EditAssociation -> {
                                    (activity as BookkeeperNavigation.NavigatorProvider).getNavigator()
                                            .showConversationsFragment(AccountInfoHolder(accountName = clickInfo.account.title,
                                                    subAccountName = clickInfo.subAccount.title))
                                }
                            }
                        }
        )
    }

    override fun retryLoading() = accountsViewModel.refreshAccounts()

    override fun getToolbarTitle(): Int = R.string.toolbar_title_accounts

    override fun getTAG() = TAG

    companion object {

        val TAG = AccountsFragment::class.java.simpleName

        fun newInstance() = AccountsFragment()
    }
}
