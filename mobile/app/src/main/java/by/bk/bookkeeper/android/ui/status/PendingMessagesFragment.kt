package by.bk.bookkeeper.android.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.databinding.FragmentPendingSmsBinding
import by.bk.bookkeeper.android.fragmentScopeViewModel
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
class PendingMessagesFragment : BaseFragment() {

    private var _binding: FragmentPendingSmsBinding? = null
    private val binding get() = _binding!!

    private val pendingMessagesViewModel: PendingMessagesViewModel by fragmentScopeViewModel()
    private val smsAdapter by lazy { PendingMessagesAdapter() }
    private val pushAdapter by lazy { PendingMessagesAdapter() }
    private val dateFormat = getListItemDateFormat()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPendingSmsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerPendingSms.run {
            adapter = smsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.recyclerPendingPushes.run {
            adapter = pushAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        binding.buttonSendPendingSms.setOnClickListener {
            pendingMessagesViewModel.sendMessagesToServer(SourceType.SMS)
        }
        binding.buttonSendPendingPushes.setOnClickListener {
            pendingMessagesViewModel.sendMessagesToServer(SourceType.PUSH)
        }
        binding.buttonRefreshUnprocessedCount.setOnClickListener {
            pendingMessagesViewModel.getServerUnprocessedCount()
        }
    }

    override fun onStart() {
        super.onStart()
        subscriptionsDisposable.addAll(
            pendingMessagesViewModel.pendingMessagesMap()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { messagesMap ->
                    if (messagesMap.isEmpty()) {
                        setupDefaultUIContent()
                        return@subscribe
                    }
                    messagesMap.entries.forEach {
                        when (it.key) {
                            SourceType.SMS -> {
                                smsAdapter.setData(it.value)
                                binding.tvPendingSmsStatus.text =
                                    if (it.value.isNotEmpty()) getUnprocessedCountString(it.key, it.value.size)
                                    else getNoPendingMessagesString(it.key)
                                binding.buttonSendPendingSms.isEnabled = it.value.isNotEmpty()
                                binding.buttonSendPendingSms.alpha = if (it.value.isNotEmpty()) 1f else 0.3f
                            }
                            SourceType.PUSH -> {
                                pushAdapter.setData(it.value)
                                binding.tvPendingPushStatus.text =
                                    if (it.value.isNotEmpty()) getUnprocessedCountString(it.key, it.value.size)
                                    else getNoPendingMessagesString(it.key)
                                binding.buttonSendPendingPushes.isEnabled = it.value.isNotEmpty()
                                binding.buttonSendPendingPushes.alpha = if (it.value.isNotEmpty()) 1f else 0.3f
                            }
                            else -> {
                                //do nothing
                            }
                        }
                    }
                },
            pendingMessagesViewModel.serverUnprocessedCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response ->
                    response.count?.let {
                        binding.tvUnprocessedMessagesCount.text = getString(R.string.msg_sms_status_server_unprocessed_count, it)
                    }
                    response.receivedDateMillis?.let {
                        binding.tvUnprocessedResponseDate.text = getString(
                            R.string.msg_sms_status_server_unprocessed_response_date_time,
                            dateFormat.format(it)
                        )
                    }
                },
            pendingMessagesViewModel.messagesLoadingState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { dataStatus ->
                    when (dataStatus) {
                        is DataStatus.Success -> Toast.makeText(context, getString(R.string.msg_operation_successful), Toast.LENGTH_SHORT).show()
                        is DataStatus.Error -> showErrorSnackbar(dataStatus.failure)
                        else -> {
                            //do nothing
                        }
                    }
                })
    }

    private fun getUnprocessedCountString(sourceType: SourceType, count: Int): String = when (sourceType) {
        SourceType.SMS -> getString(R.string.msg_sms_status_pending_sms_count, count)
        SourceType.PUSH -> getString(R.string.msg_push_status_pending_push_count, count)
        else -> ""
    }

    private fun getNoPendingMessagesString(sourceType: SourceType): String = when (sourceType) {
        SourceType.SMS -> getString(R.string.msg_sms_status_no_pending_sms)
        SourceType.PUSH -> getString(R.string.msg_push_status_no_pending_pushes)
        else -> ""
    }

    private fun setupDefaultUIContent() {
        binding.tvPendingSmsStatus.text = getNoPendingMessagesString(SourceType.SMS)
        binding.tvPendingPushStatus.text = getNoPendingMessagesString(SourceType.PUSH)
        binding.buttonSendPendingSms.isEnabled = false
        binding.buttonSendPendingSms.alpha = 0.3f
        binding.buttonSendPendingPushes.isEnabled = false
        binding.buttonSendPendingPushes.alpha = 0.3f
        smsAdapter.setData(emptyList())
        pushAdapter.setData(emptyList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun retryLoading() {
        // do nothing
    }

    override fun getFragmentTag(): String = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_messages_status

    override fun showToolbarBackButton(): Boolean = true

    companion object {
        internal val TAG = PendingMessagesFragment::class.java.simpleName
        fun newInstance() = PendingMessagesFragment()
    }
}
