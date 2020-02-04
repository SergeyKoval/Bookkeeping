package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_sms.*

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/
class SMSListFragment : BaseFragment() {

    private val associationViewModel: AssociationViewModel by activityScopeViewModel()
    private val smsAdapter by lazy { SMSAdapter() }

    private var threadId: Long = 0
    private lateinit var accountInfoHolder: AccountInfoHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        threadId = arguments?.getLong(ARG_THREAD_ID)
                ?: throw  IllegalStateException("SMS Thread ID can not be null")
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
                ?: throw  IllegalStateException("Account info can not be null")
        associationViewModel.loadInboxSMSByThreadId(threadId)
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
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                associationViewModel.sms()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { smsList ->
                            smsAdapter.setData(smsList)
                        },
                associationViewModel.smsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            if (dataStatus is DataStatus.Error) {
                                showErrorSnackbar(dataStatus.failure)
                            }
                        }
        )
    }

    override fun retryLoading() {
        associationViewModel.loadInboxSMSByThreadId(threadId)
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
