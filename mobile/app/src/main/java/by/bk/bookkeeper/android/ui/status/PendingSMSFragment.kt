package by.bk.bookkeeper.android.ui.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.fragmentScopeViewModel
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_pending_sms.*

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/
class PendingSMSFragment : BaseFragment() {

    private val pendingSmsViewModel: PendingSmsViewModel by fragmentScopeViewModel()
    private val smsAdapter by lazy { PendingSMSAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pending_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_pending_sms.run {
            adapter = smsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        button_send_sms.setOnClickListener {
            pendingSmsViewModel.sendSmsToServer()
        }
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                pendingSmsViewModel.pendingSms()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { smsList ->
                            smsAdapter.setData(smsList)
                            tv_pending_status.text = if (smsList.isNotEmpty()) getString(R.string.msg_sms_status_pending_sms_count, smsList.size)
                            else getString(R.string.msg_sms_status_no_pending_sms)
                            button_send_sms.isEnabled = smsList.isNotEmpty()
                            button_send_sms.alpha = if (smsList.isNotEmpty()) 1f else 0.3f
                        },
                pendingSmsViewModel.smsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            when (dataStatus) {
                                is DataStatus.Success -> Toast.makeText(context, getString(R.string.msg_operation_successful), Toast.LENGTH_SHORT).show()
                                is DataStatus.Error -> showErrorSnackbar(dataStatus.failure)
                            }
                        })
    }

    override fun retryLoading() = pendingSmsViewModel.sendSmsToServer()

    override fun getTAG() = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_sms_status

    companion object {

        val TAG = PendingSMSFragment::class.java.simpleName
        fun newInstance() = PendingSMSFragment()
    }
}
