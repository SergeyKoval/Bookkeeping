package by.bk.bookkeeper.android.ui.accounts

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.SubAccount
import by.bk.bookkeeper.android.ui.BaseViewHolder
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_account.view.account_title
import kotlinx.android.synthetic.main.item_account.view.recycler_sub_account
import kotlinx.android.synthetic.main.item_association.view.iv_action_edit_association
import kotlinx.android.synthetic.main.item_association.view.tv_sender
import kotlinx.android.synthetic.main.item_association.view.tv_sms_body_template
import kotlinx.android.synthetic.main.item_association.view.tv_source_type
import kotlinx.android.synthetic.main.item_sub_account.view.iv_action_add_association
import kotlinx.android.synthetic.main.item_sub_account.view.recycler_association
import kotlinx.android.synthetic.main.item_sub_account.view.tv_sub_account_title

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountViewHolder(private val clicksSubject: PublishSubject<SubAccountRecyclerClick>, view: View)
    : BaseViewHolder<Account>(view) {

    private val title: TextView = itemView.account_title
    private val recyclerSubAccount: RecyclerView = itemView.recycler_sub_account

    override fun setItem(item: Account?) {
        item ?: return
        title.text = item.title
        recyclerSubAccount.run {
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
    private val associationRecyclerView: RecyclerView = itemView.recycler_association
    private val actionIcon: ImageView = itemView.iv_action_add_association

    override fun setItem(item: SubAccount?) {
        item ?: return
        titleTextView.text = item.title
        actionIcon.setOnClickListener {
            clickObservable.onNext(SubAccountRecyclerClick.AddAssociation(account, item))
        }
        if (item.associations.isNotEmpty()) {
            associationRecyclerView.adapter = AssociationsAdapter(account, item, item.associations, clickObservable)
        }
    }
}

/**
 *  Created by Evgenia Grinkevich on 01, October, 2020
 **/
class AssociationViewHolder(private val account: Account,
                            private val subAccount: SubAccount,
                            private val clickObservable: PublishSubject<SubAccountRecyclerClick>,
                            view: View)
    : BaseViewHolder<Association>(view) {

    private val sourceTypeTextView: TextView = itemView.tv_source_type
    private val senderTextView: TextView = itemView.tv_sender
    private val templateTextView: TextView = itemView.tv_sms_body_template
    private val actionIcon: ImageView = itemView.iv_action_edit_association
    private val actionsPopup: PopupMenu = createActionsPopup(actionIcon, R.menu.association_item_action)

    init {
        actionIcon.setOnClickListener {
            showPopupMenuWithIcons(actionsPopup.menu as MenuBuilder, it)
        }
    }

    override fun setItem(item: Association?) {
        item ?: return
        sourceTypeTextView.text = SourceType.getUiString(itemView.context, item.sourceType)
        senderTextView.text = itemView.context.getString(R.string.association_sender, item.sender)
        templateTextView.visibility = if (!item.smsBodyTemplate.isNullOrEmpty()) View.VISIBLE else View.GONE
        templateTextView.text = itemView.context.getString(R.string.association_template, item.smsBodyTemplate)
        actionsPopup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_remove -> {
                    clickObservable.onNext(SubAccountRecyclerClick.RemoveAssociation(account, subAccount, item))
                    return@setOnMenuItemClickListener true
                }
                R.id.action_edit -> {
                    clickObservable.onNext(SubAccountRecyclerClick.EditAssociation(account, subAccount, item, item.sourceType))
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
    }
}