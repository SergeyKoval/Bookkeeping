package by.bk.bookkeeper.android.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.databinding.ItemSubAccountBinding
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.SubAccount
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class SubAccountsAdapter(private val account: Account,
                         private val subAccounts: List<SubAccount>,
                         private val clicksSubject: PublishSubject<SubAccountRecyclerClick>
) : RecyclerView.Adapter<SubAccountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubAccountViewHolder =
            SubAccountViewHolder(account, clicksSubject,
                    ItemSubAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = subAccounts.size

    override fun onBindViewHolder(holder: SubAccountViewHolder, position: Int) {
        holder.setItem(subAccounts[position])
    }
}