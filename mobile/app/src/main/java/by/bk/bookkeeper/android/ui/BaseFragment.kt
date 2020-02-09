package by.bk.bookkeeper.android.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.action
import by.bk.bookkeeper.android.actionSnackbar
import by.bk.bookkeeper.android.network.wrapper.FailureWrapper
import com.google.android.material.snackbar.Snackbar
import io.reactivex.disposables.CompositeDisposable

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
abstract class BaseFragment : Fragment() {

    protected val subscriptionsDisposable: CompositeDisposable = CompositeDisposable()
    private var errorSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(getToolbarTitle())
    }

    protected fun showErrorSnackbar(failure: FailureWrapper) {
        activity?.findViewById<View>(R.id.accounting_root_coordinator_layout)?.actionSnackbar(
                messageRes = failure.messageStringRes,
                length = Snackbar.LENGTH_INDEFINITE) {
            action(R.string.action_retry_loading) {
                retryLoading()
            }
        }.also { errorSnackbar = it }
    }

    override fun onStop() {
        subscriptionsDisposable.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        errorSnackbar?.dismiss()
        errorSnackbar = null
        super.onDestroyView()
    }

    abstract fun retryLoading()

    abstract fun getTAG(): String
    @StringRes
    abstract fun getToolbarTitle(): Int
}
