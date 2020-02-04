package by.bk.bookkeeper.android.ui.accounts

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.SubAccount
import by.bk.bookkeeper.android.ui.BaseViewHolder
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_account.view.*
import kotlinx.android.synthetic.main.item_sub_account.view.*

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountViewHolder(private val clicksSubject: PublishSubject<SubAccountRecyclerClick>, view: View) : BaseViewHolder<Account>(view) {

    private val title: TextView = itemView.account_title
    private val recyclerSubAccount: RecyclerView = itemView.recycler_sub_account

    override fun setItem(item: Account?) {
        item ?: return
        title.text = item.title
        recyclerSubAccount.run {
            layoutManager = LinearLayoutManager(itemView.context)
            adapter = SubAccountsAdapter(item, item.subAccounts, clicksSubject)
        }
    }
}

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class SubAccountViewHolder(private val account: Account,
                           private val clickObservable: PublishSubject<SubAccountRecyclerClick>,
                           view: View)
    : BaseViewHolder<SubAccount>(view) {

    private val titleTextView: TextView = itemView.tv_sub_account_title
    private val senderTextView: TextView = itemView.tv_sender
    private val smsBodyTemplateTextView: TextView = itemView.tv_sms_body_template
    private val actionIcon: ImageView = itemView.iv_action_association

    private val actionsPopup: PopupMenu = createActionsPopup(actionIcon, R.menu.account_item_action)

    init {
        actionIcon.setOnClickListener {
            showPopupMenuWithIcons(actionsPopup.menu as MenuBuilder, it)
        }
    }

    override fun setItem(item: SubAccount?) {
        item ?: return
        titleTextView.text = item.title
        senderTextView.visibility = if (item.association != null) View.VISIBLE else View.GONE
        smsBodyTemplateTextView.visibility = if (!item.association?.smsBodyTemplate.isNullOrEmpty()) View.VISIBLE else View.GONE
        item.association?.let { association ->
            senderTextView.text = itemView.context.getString(R.string.association_sender, association.sender)
            smsBodyTemplateTextView.text = association.smsBodyTemplate
        }
        actionsPopup.menu.findItem(R.id.action_add).isVisible = item.association == null
        actionsPopup.menu.findItem(R.id.action_remove).isVisible = item.association != null
        actionsPopup.menu.findItem(R.id.action_edit).isVisible = item.association != null
        actionsPopup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    clickObservable.onNext(SubAccountRecyclerClick.AddAssociation(account, item))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_remove -> {
                    clickObservable.onNext(SubAccountRecyclerClick.RemoveAssociation(account, item))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_edit -> {
                    clickObservable.onNext(SubAccountRecyclerClick.EditAssociation(account, item))
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }

    }
}