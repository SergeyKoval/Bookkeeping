package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.databinding.FragmentSmsBinding
import by.bk.bookkeeper.android.hideKeyboard
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.accounts.AccountsViewModel
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/
class SMSListFragment : BaseFragment() {

    private var _binding: FragmentSmsBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerSms.run {
            layoutManager = LinearLayoutManager(context)
            adapter = smsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.smsSwipeRefresh.setOnRefreshListener {
            inboxSmsViewModel.loadInboxSMSByThreadId(conversation.threadId)
        }
        binding.btnAssociate.setOnClickListener {
            accountViewModel.addAssociation(
                AssociationRequest(
                    accountName = accountInfoHolder.accountName,
                    subAccountName = accountInfoHolder.subAccountName,
                    sender = conversation.sender.address,
                    associationString = binding.editTextTemplate.text?.toString(),
                    source = SourceType.SMS
                )
            )
            hideKeyboard(binding.statusLayoutRoot)
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
                    binding.smsSwipeRefresh.isRefreshing = dataStatus is DataStatus.Loading
                    if (dataStatus is DataStatus.Error) {
                        showErrorSnackbar(dataStatus.failure)
                    }
                },
            binding.editTextTemplate.textChanges()
                .skipInitialValue()
                .subscribe { binding.textInputTemplate.error = null }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun retryLoading() {
        inboxSmsViewModel.loadInboxSMSByThreadId(conversation.threadId)
    }

    override fun getFragmentTag(): String = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating_sms

    override fun showToolbarBackButton(): Boolean = true

    companion object {

        private val TAG = SMSListFragment::class.java.simpleName
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
