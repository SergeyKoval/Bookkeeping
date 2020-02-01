package by.bk.bookkeeper.android.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.ui.SubAccountRecyclerClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 31, January, 2020
 **/
class AccountsAdapter : RecyclerView.Adapter<AccountViewHolder>() {

    private var accounts: List<Account> = arrayListOf()

    private val clicksSubject = PublishSubject.create<SubAccountRecyclerClick>()
    fun subAccountItemClick(): Observable<SubAccountRecyclerClick> = clicksSubject

    fun setData(accounts: List<Account>) {
        this.accounts = accounts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder =
            AccountViewHolder(clicksSubject, LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false))

    override fun getItemCount(): Int = accounts.size

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.setItem(accounts[position])
    }
}