package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.hideKeyboard
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.accounts.AccountsViewModel
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_sms.*

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/
class SMSListFragment : BaseFragment() {

    private val inboxSmsViewModel: InboxSmsViewModel by activityScopeViewModel()
    private val accountViewModel: AccountsViewModel by activityScopeViewModel()
    private val smsAdapter by lazy { SMSAdapter() }

    private var threadId: Long = 0
    private lateinit var accountInfoHolder: AccountInfoHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        threadId = arguments?.getLong(ARG_THREAD_ID)
                ?: throw  IllegalStateException("SMS Thread ID can not be null")
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
                ?: throw  IllegalStateException("Account info can not be null")
        inboxSmsViewModel.loadInboxSMSByThreadId(threadId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_sms.run {
            layoutManager = LinearLayoutManager(context)
            adapter = smsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        sms_swipe_refresh.setOnRefreshListener {
            inboxSmsViewModel.loadInboxSMSByThreadId(threadId)
        }
        btn_associate.setOnClickListener {
            if (edit_text_template.text.isNullOrEmpty()) {
                text_input_template.error = getString(R.string.err_empty_template)
            } else {
                accountViewModel.addAssociation(AssociationRequest(accountName = accountInfoHolder.accountName,
                        subAccountName = accountInfoHolder.subAccountName,
                        sender = smsAdapter.getSenderAddress(),
                        associationString = edit_text_template.text.toString()))
                hideKeyboard(sms_layout_root)
                (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.popBackStackToRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                inboxSmsViewModel.sms()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { smsList ->
                            smsAdapter.setData(smsList)
                        },
                inboxSmsViewModel.smsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            sms_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                            if (dataStatus is DataStatus.Error) {
                                showErrorSnackbar(dataStatus.failure)
                            }
                        },
                edit_text_template.textChanges()
                        .skipInitialValue()
                        .subscribe { text_input_template.error = null }
        )
    }

    override fun retryLoading() {
        inboxSmsViewModel.loadInboxSMSByThreadId(threadId)
    }

    override fun getTAG() = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating

    companion object {

        val TAG = SMSListFragment::class.java.simpleName
        private const val ARG_INFO_HOLDER = "arg_info_holder"
        private const val ARG_THREAD_ID = "arg_thread_id"

        fun newInstance(threadId: Long, infoHolder: AccountInfoHolder) = SMSListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_INFO_HOLDER, infoHolder)
                putLong(ARG_THREAD_ID, threadId)
            }
        }
    }
}
