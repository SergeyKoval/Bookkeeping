package by.bk.bookkeeper.android.ui.association

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.hideKeyboard
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_sms_asociation.*

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class SMSAssociationFragment : BaseFragment() {

    private val inboxSmsViewModel: InboxSmsViewModel by activityScopeViewModel()
    private val conversationAdapter by lazy { ConversationFilterableAdapter() }

    private lateinit var accountInfoHolder: AccountInfoHolder
    private var userQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userQuery = savedInstanceState?.getString(KEY_QUERY)
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
                ?: throw  IllegalStateException("Account info can not be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sms_asociation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_conversations.run {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        conversations_swipe_refresh.setOnRefreshListener {
            inboxSmsViewModel.reloadConversations()
        }
    }

    override fun onResume() {
        super.onResume()
        if (RxPermissions(this).isGranted(Manifest.permission.READ_SMS)
                && RxPermissions(this).isGranted(Manifest.permission.READ_CONTACTS)) {
            proceedToSMSHandling()
        } else {
            Toast.makeText(context, getString(R.string.err_no_permissions), Toast.LENGTH_SHORT).show()
            (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.activity_accounting_toolbar_menu, menu)
        (menu.findItem(R.id.toolbar_search).actionView as SearchView).apply {
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    conversationAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    conversationAdapter.filter.filter(newText)
                    userQuery = newText
                    return false
                }
            })
            userQuery?.let {
                setQuery(it, true)
                isIconified = false
                clearFocus()
                hideKeyboard(rootView)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        userQuery?.let { outState.putString(KEY_QUERY, it) }
    }

    private fun proceedToSMSHandling() {
        subscriptionsDisposable.addAll(
                inboxSmsViewModel.conversations()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { data ->
                            conversationAdapter.setData(data)
                        },
                inboxSmsViewModel.conversationsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            conversations_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                            if (dataStatus is DataStatus.Error) {
                                showErrorSnackbar(dataStatus.failure)
                            }
                        },
                conversationAdapter.itemClick()
                        .subscribe {
                            hideKeyboard(view?.rootView)
                            (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.showSmsListFragment(
                                    conversation = conversationAdapter.getItem(it.position),
                                    accountInfoHolder = accountInfoHolder)
                        }
        )
    }

    override fun retryLoading() {
        inboxSmsViewModel.reloadConversations()
    }

    override fun getFragmentTag(): String = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating_sms

    override fun showToolbarBackButton(): Boolean = true

    companion object {

        private val TAG = SMSAssociationFragment::class.java.simpleName
        private const val KEY_QUERY = "key_query"
        private const val ARG_INFO_HOLDER = "arg_info_holder"

        fun newInstance(infoHolder: AccountInfoHolder) = SMSAssociationFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_INFO_HOLDER, infoHolder) }
        }
    }
}
