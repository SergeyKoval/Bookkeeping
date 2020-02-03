package by.bk.bookkeeper.android.ui.association

import android.Manifest
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.activityScopeViewModel
import by.bk.bookkeeper.android.hideKeyboard
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
    private val conversationAdapter by lazy { ConversationAdapter() }

    private var userQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userQuery = savedInstanceState?.getString(KEY_QUERY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_asociation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_conversations.run {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        conversations_swipe_refresh.setOnRefreshListener {
            associationViewModel.reloadConversations()
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.activity_accounting_toolbar_menu, menu)
        (menu.findItem(R.id.toolbar_search).actionView as SearchView).apply {
            maxWidth = Int.MAX_VALUE
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    conversationAdapter.filter.filter(query)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    conversationAdapter.filter.filter(newText)
                    userQuery = newText
                    return false
                }
            })
            userQuery?.let {
                setQuery(it, true)
                isIconified = false
                hideKeyboard(rootView)
            }
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        userQuery?.let { outState.putString(KEY_QUERY, it) }
    }

    private fun proceedToSMSHandling() {
        subscriptionsDisposable.addAll(
                associationViewModel.conversations()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { data ->
                            conversationAdapter.setData(data)
                        },
                associationViewModel.conversationsLoadingState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { dataStatus ->
                            conversations_swipe_refresh.isRefreshing = dataStatus is DataStatus.Loading
                            if (dataStatus is DataStatus.Error) {
                                showErrorSnackbar(dataStatus.failure)
                            }
                        }
        )
    }

    override fun retryLoading() {
        associationViewModel.reloadConversations()
    }

    override fun getTAG() = TAG

    override fun getToolbarTitle(): Int = R.string.toolbar_title_associating

    companion object {

        val TAG = AssociationsFragment::class.java.simpleName
        private const val KEY_QUERY = "key_query"

        fun newInstance() = AssociationsFragment()

    }
}
