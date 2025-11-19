package by.bk.bookkeeper.android.ui.association

import by.bk.bookkeeper.android.databinding.ItemMessageBodyBinding
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.ui.BaseViewHolder
import java.util.Date

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/

class SMSViewHolder(private val binding: ItemMessageBodyBinding) : BaseViewHolder<SMS>(binding.root) {

    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: SMS?) {
        item ?: return
        binding.tvBody.text = item.body
        binding.tvBody.setTextIsSelectable(true)
        binding.tvBody.isLongClickable = true
        binding.tvDateSent.text = dateFormat.format(Date(item.dateReceived))
    }
}
