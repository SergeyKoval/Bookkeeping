package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.databinding.FragmentPushAssociationBinding
import by.bk.bookkeeper.android.fragmentScopeViewModel
import by.bk.bookkeeper.android.hideKeyboard
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.request.AssociationRequest
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import by.bk.bookkeeper.android.ui.accounts.AccountsViewModel
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges

/**
 *  Created by Evgenia Grinkevich on 20, May, 2023
 **/
class PushAssociationFragment : BaseFragment() {

    private var _binding: FragmentPushAssociationBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountInfoHolder: AccountInfoHolder

    private val pushAssociationViewModel: PushAssociationViewModel by fragmentScopeViewModel()
    private val accountsViewModel: AccountsViewModel by activityScopeViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
            ?: throw IllegalStateException("Account info can not be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPushAssociationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        subscriptionsDisposable.addAll(
            binding.btnAssociatePush.clicks().subscribe {
                val userInput = binding.editTextPushPackage.text?.toString()?.trim()
                if (pushAssociationViewModel.validatePackageInput(userInput)) {
                    accountsViewModel.addAssociation(
                        AssociationRequest(
                            accountName = accountInfoHolder.accountName,
                            subAccountName = accountInfoHolder.subAccountName,
                            sender = binding.editTextPushPackage.text?.toString() ?: "",
                            associationString = binding.editTextPushContent.text?.toString(),
                            source = SourceType.PUSH
                        )
                    )
                    hideKeyboard(binding.pushLayoutRoot)
                    (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()?.popBackStackToRoot()
                } else {
                    binding.editTextPushPackage.error = getString(R.string.push_package_invalid_name)
                    binding.editTextPushPackage.requestFocus()
                }
            },
            binding.editTextPushPackage.textChanges()
                .skipInitialValue()
                .subscribe { binding.editTextPushPackage.error = null }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
