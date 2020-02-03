package by.bk.bookkeeper.android.ui.association

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.RecyclerClick
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

class ConversationAdapter : RecyclerView.Adapter<SenderViewHolder>() {

    private var conversations: List<Conversation> = arrayListOf()

    private val clicksSubject = PublishSubject.create<RecyclerClick>()
    fun itemClick(): Observable<RecyclerClick> = clicksSubject

    fun setData(conversations: List<Conversation>) {
        this.conversations = conversations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SenderViewHolder =
            SenderViewHolder(clicksSubject, LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false))

    override fun getItemCount(): Int = conversations.size

    override fun onBindViewHolder(holder: SenderViewHolder, position: Int) {
        holder.setItem(conversations[position])
    }
}