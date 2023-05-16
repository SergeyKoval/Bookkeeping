package by.bk.bookkeeper.android.ui.association

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.ui.BaseFragment
import by.bk.bookkeeper.android.ui.BookkeeperNavigation
import kotlinx.android.synthetic.main.fragment_asociation_type.container_push
import kotlinx.android.synthetic.main.fragment_asociation_type.container_sms

/**
 *  Created by Evgenia Grinkevich on 16, May, 2023
 **/
class AssociationTypeFragment : BaseFragment() {

    private lateinit var accountInfoHolder: AccountInfoHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountInfoHolder = arguments?.getParcelable(ARG_INFO_HOLDER)
            ?: throw IllegalStateException("Account info can not be null")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asociation_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        container_sms.setOnClickListener {
            getMainNavigator(activity)?.showAssociationFragment(AssociationType.SMS, accountInfoHolder)
        }
        container_push.setOnClickListener {
            getMainNavigator(activity)?.showAssociationFragment(AssociationType.PUSH, accountInfoHolder)
        }
    }

    override fun retryLoading() {
        //do nothing
    }

    override fun getFragmentTag(): String = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_association_type

    override fun showToolbarBackButton(): Boolean = true

    private fun getMainNavigator(activity: FragmentActivity?): BookkeeperNavigation.Navigator? =
        (activity as? BookkeeperNavigation.NavigatorProvider)?.getNavigator()

    companion object {

        private val TAG = AssociationTypeFragment::class.java.simpleName
        private const val ARG_INFO_HOLDER = "arg_info_holder"

        fun newInstance(infoHolder: AccountInfoHolder) = AssociationTypeFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_INFO_HOLDER, infoHolder) }
        }
    }
}