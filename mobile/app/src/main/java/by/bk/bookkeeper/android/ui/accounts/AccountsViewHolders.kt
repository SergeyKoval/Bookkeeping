package by.bk.bookkeeper.android.ui.accounts

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.databinding.ItemAccountBinding
import by.bk.bookkeeper.android.databinding.ItemAssociationBinding
import by.bk.bookkeeper.android.databinding.ItemSubAccountBinding
import by.bk.bookkeeper.android.network.dto.SourceType
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.SubAccount
import by.bk.bookkeeper.android.ui.BaseViewHolder
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountViewHolder(private val clicksSubject: PublishSubject<SubAccountRecyclerClick>, private val binding: ItemAccountBinding)
    : BaseViewHolder<Account>(binding.root) {

    override fun setItem(item: Account?) {
        item ?: return
        binding.accountTitle.text = item.title
        binding.recyclerSubAccount.run {
            adapter = SubAccountsAdapter(item, item.subAccounts, clicksSubject)
        }
    }
}

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class SubAccountViewHolder(private val account: Account,
                           private val clickObservable: PublishSubject<SubAccountRecyclerClick>,
                           private val binding: ItemSubAccountBinding)
    : BaseViewHolder<SubAccount>(binding.root) {

    override fun setItem(item: SubAccount?) {
        item ?: return
        binding.tvSubAccountTitle.text = item.title
        binding.ivActionAddAssociation.setOnClickListener {
            clickObservable.onNext(SubAccountRecyclerClick.AddAssociation(account, item))
        }
        if (item.associations.isNotEmpty()) {
            binding.recyclerAssociation.adapter = AssociationsAdapter(account, item, item.associations, clickObservable)
        }
    }
}

/**
 *  Created by Evgenia Grinkevich on 01, October, 2020
 **/
class AssociationViewHolder(private val account: Account,
                            private val subAccount: SubAccount,
                            private val clickObservable: PublishSubject<SubAccountRecyclerClick>,
                            private val binding: ItemAssociationBinding)
    : BaseViewHolder<Association>(binding.root) {

    private val actionsPopup: PopupMenu = createActionsPopup(binding.ivActionEditAssociation, R.menu.association_item_action)

    init {
        binding.ivActionEditAssociation.setOnClickListener {
            showPopupMenuWithIcons(actionsPopup.menu as MenuBuilder, it)
        }
    }

    override fun setItem(item: Association?) {
        item ?: return
        binding.tvSourceType.text = SourceType.getUiString(itemView.context, item.sourceType)
        binding.tvSender.text = itemView.context.getString(R.string.association_sender, item.sender)
        binding.tvSmsBodyTemplate.visibility = if (!item.smsBodyTemplate.isNullOrEmpty()) View.VISIBLE else View.GONE
        binding.tvSmsBodyTemplate.text = itemView.context.getString(R.string.association_template, item.smsBodyTemplate)
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