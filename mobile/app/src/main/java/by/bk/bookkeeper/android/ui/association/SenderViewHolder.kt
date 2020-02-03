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

class SenderViewHolder(private val clicksSubject: PublishSubject<RecyclerClick>, view: View) : BaseViewHolder<Conversation>(view) {

    private val senderName: TextView = itemView.tv_sender_name
    private val phoneNumber: TextView = itemView.tv_phone_number

    override fun setItem(item: Conversation?) {
        item?.sender ?: return
        if (item.sender.addressBookDisplayableName != null) {
            senderName.text = item.sender.addressBookDisplayableName
            phoneNumber.text = item.sender.address
            phoneNumber.visibility = View.VISIBLE
        } else {
            senderName.text = item.sender.address
            phoneNumber.visibility = View.GONE
        }
    }
}
