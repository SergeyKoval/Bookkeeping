package by.bk.bookkeeper.android.ui.association

import android.view.View
import android.widget.TextView
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.BaseViewHolder
import by.bk.bookkeeper.android.ui.RecyclerClick
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_conversation.view.*

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

class ConversationViewHolder(private val clicksSubject: PublishSubject<RecyclerClick>, view: View) : BaseViewHolder<Conversation>(view) {

    private val senderNameTextView: TextView = itemView.tv_sender_name
    private val phoneNumberTextView: TextView = itemView.tv_phone_number
    private val snippetTextView: TextView = itemView.tv_snippet

    override fun setItem(item: Conversation?) {
        val sender = item?.sender ?: return
        snippetTextView.text = sender.snippet
        if (item.sender.addressBookDisplayableName.isNotEmpty()) {
            senderNameTextView.text = sender.addressBookDisplayableName
            phoneNumberTextView.text = sender.address
            phoneNumberTextView.visibility = View.VISIBLE
        } else {
            senderNameTextView.text = sender.address
            phoneNumberTextView.visibility = View.GONE
        }

    }
}
