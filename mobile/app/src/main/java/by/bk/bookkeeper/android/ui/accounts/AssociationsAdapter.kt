package by.bk.bookkeeper.android.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.network.response.Account
import by.bk.bookkeeper.android.network.response.Association
import by.bk.bookkeeper.android.network.response.SubAccount
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 01, October, 2020
 **/
class AssociationsAdapter(private val account: Account,
                          private val subAccount: SubAccount,
                          private val associations: List<Association>,
                          private val clicksSubject: PublishSubject<SubAccountRecyclerClick>
) : RecyclerView.Adapter<AssociationViewHolder>() {

    override fun onBindViewHolder(holder: AssociationViewHolder, position: Int) {
        holder.setItem(associations[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssociationViewHolder =
            AssociationViewHolder(account, subAccount, clicksSubject,
                    LayoutInflater.from(parent.context).inflate(R.layout.item_association, parent, false))

    override fun getItemCount(): Int = associations.size

}