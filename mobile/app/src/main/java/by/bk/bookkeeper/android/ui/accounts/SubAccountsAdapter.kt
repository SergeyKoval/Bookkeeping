package by.bk.bookkeeper.android.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.response.SubAccount
import by.bk.bookkeeper.android.ui.RecyclerClick
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class SubAccountsAdapter(private val subAccounts: List<SubAccount>,
                         private val clicksSubject: PublishSubject<RecyclerClick>) : RecyclerView.Adapter<SubAccountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubAccountViewHolder =
            SubAccountViewHolder(clicksSubject, LayoutInflater.from(parent.context).inflate(R.layout.item_sub_account, parent, false))

    override fun getItemCount(): Int = subAccounts.size

    override fun onBindViewHolder(holder: SubAccountViewHolder, position: Int) {
        holder.setItem(subAccounts[position])
    }
}