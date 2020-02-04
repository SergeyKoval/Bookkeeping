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
import by.bk.bookkeeper.android.sms.Conversation
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

    private lateinit var accountInfoHolder: AccountInfoHolder
    private lateinit var conversation: Conversation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
                ?: throw  IllegalStateException("Account info can not be null")
        conversation = arguments?.getParcelable(ARG_CONVERSATION)
                ?: throw  IllegalStateException("Conversation can not be null")
        inboxSmsViewModel.loadInboxSMSByThreadId(conversation.threadId)
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
            inboxSmsViewModel.loadInboxSMSByThreadId(conversation.threadId)
        }
        btn_associate.setOnClickListener {
            accountViewModel.addAssociation(AssociationRequest(accountName = accountInfoHolder.accountName,
                    subAccountName = accountInfoHolder.subAccountName,
                    sender = conversation.sender.address,
                    associationString = edit_text_template.text?.toString()))
            hideKeyboard(sms_layout_root)
            (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.popBackStackToRoot()
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
        inboxSmsViewModel.loadInboxSMSByThreadId(conversation.threadId)
    }

    override fun getTAG() = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating

    companion object {

        val TAG = SMSListFragment::class.java.simpleName
        private const val ARG_INFO_HOLDER = "arg_info_holder"
        private const val ARG_CONVERSATION = "arg_conversation"

        fun newInstance(conversation: Conversation, infoHolder: AccountInfoHolder) = SMSListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_INFO_HOLDER, infoHolder)
                putParcelable(ARG_CONVERSATION, conversation)
            }
        }
    }
}
