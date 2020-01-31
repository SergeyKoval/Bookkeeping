package by.bk.bookkeeper.android.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.ui.BaseFragment

class AccountsFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_accounts, container, false)
    }

    override fun getTAG() = TAG

    companion object {

        val TAG = AccountsFragment::class.java.simpleName

        fun newInstance() = AccountsFragment()
    }
}
