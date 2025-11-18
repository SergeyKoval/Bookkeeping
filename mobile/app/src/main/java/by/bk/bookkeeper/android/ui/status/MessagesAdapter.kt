package by.bk.bookkeeper.android.ui.status

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.databinding.ItemMessageDetailedBinding
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.network.request.ProcessedMessage
import by.bk.bookkeeper.android.ui.BaseViewHolder
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
        PendingMessagesViewHolder(ItemMessageDetailedBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: PendingMessagesViewHolder, position: Int) {
        holder.setItem(messages[position])
    }
}

class PendingMessagesViewHolder(private val binding: ItemMessageDetailedBinding) : BaseViewHolder<ProcessedMessage>(binding.root) {

    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: ProcessedMessage?) {
        item ?: return
        binding.tvSender.text = item.deviceMessage.sender
        binding.tvBody.text = item.deviceMessage.fullText
        binding.tvAccountSubaccount.text = itemView.context.getString(
            R.string.msg_sms_status_account_subaccount_info,
            item.account, item.subAccount
        )
        binding.tvDateSent.text = dateFormat.format(Date(item.deviceMessage.timestamp))
    }
}
