package by.bk.bookkeeper.android.ui.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.ui.BaseViewHolder
import kotlinx.android.synthetic.main.item_message_body.view.tv_body
import kotlinx.android.synthetic.main.item_message_body.view.tv_date_sent
import kotlinx.android.synthetic.main.item_message_detailed.view.tv_account_subaccount
import kotlinx.android.synthetic.main.item_message_detailed.view.tv_sender
import java.util.Date

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/

class PendingMessagesAdapter : RecyclerView.Adapter<PendingMessagesViewHolder>() {

    private var messages: List<ProcessedMessage> = arrayListOf()

    fun setData(sms: List<ProcessedMessage>) {
        this.messages = sms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingMessagesViewHolder =
        PendingMessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_detailed, parent, false))

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: PendingMessagesViewHolder, position: Int) {
        holder.setItem(messages[position])
    }
}

class PendingMessagesViewHolder(view: View) : BaseViewHolder<ProcessedMessage>(view) {

    private val dateSentTextView: TextView = itemView.tv_date_sent
    private val senderTextView: TextView = itemView.tv_sender
    private val bodyTextView: TextView = itemView.tv_body
    private val accountInfoTextView: TextView = itemView.tv_account_subaccount
    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: ProcessedMessage?) {
        item ?: return
        senderTextView.text = item.deviceMessage.sender
        bodyTextView.text = item.deviceMessage.fullText
        accountInfoTextView.text = itemView.context.getString(
            R.string.msg_sms_status_account_subaccount_info,
            item.account, item.subAccount
        )
        dateSentTextView.text = dateFormat.format(Date(item.deviceMessage.timestamp))
    }
}
