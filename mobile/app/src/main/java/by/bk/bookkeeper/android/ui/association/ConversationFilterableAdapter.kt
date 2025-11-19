package by.bk.bookkeeper.android.ui.association

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.databinding.ItemConversationBinding
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.RecyclerClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

class ConversationFilterableAdapter : RecyclerView.Adapter<ConversationViewHolder>(), Filterable {

    private var conversations: List<Conversation> = arrayListOf()
    private var conversationsFiltered: List<Conversation> = arrayListOf()

    private val clicksSubject = PublishSubject.create<RecyclerClick>()
    fun itemClick(): Observable<RecyclerClick> = clicksSubject

    fun setData(conversations: List<Conversation>) {
        this.conversations = conversations
        conversationsFiltered = conversations
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Conversation = conversationsFiltered[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder =
            ConversationViewHolder(clicksSubject, ItemConversationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = conversationsFiltered.size

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.setItem(conversationsFiltered[position])
    }

    @Suppress("UNCHECKED_CAST")
    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults = FilterResults().apply {
            if (!constraint.isNullOrEmpty()) {
                val filteredResults: ArrayList<Conversation> = arrayListOf()
                for (item in conversations) {
                    if (item.sender.address.contains(constraint, ignoreCase = true)
                            || item.sender.addressBookDisplayableName.contains(constraint, ignoreCase = true)
                            || item.sender.snippet.contains(constraint, ignoreCase = true)) {
                        filteredResults.add(item)
                    }
                }
                values = filteredResults
                count = filteredResults.size
            } else {
                values = conversations
                count = conversations.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            conversationsFiltered = results?.values as ArrayList<Conversation>
            notifyDataSetChanged()
        }
    }
}