package by.bk.bookkeeper.android.ui.association

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.network.wrapper.DataStatus
import by.bk.bookkeeper.android.ui.BaseFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_asociation.*

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AssociationsFragment : BaseFragment() {

    private val associationViewModel: AssociationViewModel by activityScopeViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asociation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        subscriptionsDisposable.addAll(
                RxPermissions(this)
                        .request(Manifest.permission.READ_SMS, Manifest.permission.READ_CONTACTS)
                        .subscribe { granted ->
                            if (granted) {
                                proceedToSMSHandling()
                            } else {
                                Toast.makeText(context, getString(R.string.err_no_permissions), Toast.LENGTH_SHORT).show()
                                activity?.supportFragmentManager?.popBackStack()
                            }
                        })
    }

    private fun proceedToSMSHandling() {
        subscriptionsDisposable.addAll(
                associationViewModel.sms()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                        },
                associationViewModel.storedSmsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            sms_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                        }
        )
    }

    override fun retryLoading() {}

    override fun getTAG() = TAG

    companion object {

        val TAG = AssociationsFragment::class.java.simpleName

        fun newInstance() = AssociationsFragment()

        // TODO add args
        fun show(targetFragment: Fragment) {
            targetFragment.activity?.let { activity ->
                val fragment: AssociationsFragment = activity.supportFragmentManager.findFragmentByTag(TAG) as? AssociationsFragment
                        ?: newInstance()
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.section_fragment_container, fragment, fragment.getTAG())
                        .commit()
            } ?: throw java.lang.IllegalStateException("Activity can not be null")
        }
    }
}
