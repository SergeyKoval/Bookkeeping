package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.fragmentScopeViewModel
import by.bk.bookkeeper.android.hideKeyboard
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.accounts.AccountsViewModel
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import kotlinx.android.synthetic.main.fragment_push_association.btn_associate_push
import kotlinx.android.synthetic.main.fragment_push_association.edit_text_push_content
import kotlinx.android.synthetic.main.fragment_push_association.edit_text_push_package
import kotlinx.android.synthetic.main.fragment_push_association.push_layout_root

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/
class PushAssociationFragment : BaseFragment() {

    private lateinit var accountInfoHolder: AccountInfoHolder

    private val pushAssociationViewModel: PushAssociationViewModel by fragmentScopeViewModel()
    private val accountsViewModel: AccountsViewModel by activityScopeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
            ?: throw IllegalStateException("Account info can not be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_push_association, container, false)
    }

    override fun onStart() {
        super.onStart()
        subscriptionsDisposable.addAll(
            btn_associate_push.clicks().subscribe {
                val userInput = edit_text_push_package.text?.toString()?.trim()
                if (pushAssociationViewModel.validatePackageInput(userInput)) {
                    accountsViewModel.addAssociation(
                        AssociationRequest(
                            accountName = accountInfoHolder.accountName,
                            subAccountName = accountInfoHolder.subAccountName,
                            sender = edit_text_push_package.text?.toString() ?: "",
                            associationString = edit_text_push_content.text?.toString(),
                            source = SourceType.PUSH
                        )
                    )
                    hideKeyboard(push_layout_root)
                    (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.popBackStackToRoot()
                } else {
                    edit_text_push_package.error = getString(R.string.push_package_invalid_name)
                    edit_text_push_package.requestFocus()
                }
            },
            edit_text_push_package.textChanges()
                .skipInitialValue()
                .subscribe { edit_text_push_package.error = null }
        )
    }

    override fun onStop() {
        subscriptionsDisposable.clear()
        super.onStop()
    }

    override fun getFragmentTag(): String = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating_push

    override fun retryLoading() {
        // do nothing
    }

    override fun showToolbarBackButton(): Boolean = true

    companion object {

        private val TAG = PushAssociationFragment::class.java.simpleName
        private const val ARG_INFO_HOLDER = "arg_info_holder"

        fun newInstance(infoHolder: AccountInfoHolder) = PushAssociationFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_INFO_HOLDER, infoHolder) }
        }
    }
}
