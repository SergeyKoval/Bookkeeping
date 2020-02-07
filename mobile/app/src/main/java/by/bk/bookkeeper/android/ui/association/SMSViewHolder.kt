package by.bk.bookkeeper.android.ui.association

import android.view.View
import android.widget.TextView
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.ui.BaseViewHolder
import kotlinx.android.synthetic.main.item_sms_body.view.*
import java.util.*

/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/

class SMSViewHolder(view: View) : BaseViewHolder<SMS>(view) {

    private val smsBodyTextView: TextView = itemView.tv_sms_body
    private val dateSentTextView: TextView = itemView.tv_date_sent
    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: SMS?) {
        item ?: return
        smsBodyTextView.text = item.body
        smsBodyTextView.setTextIsSelectable(true)
        smsBodyTextView.isLongClickable = true
        dateSentTextView.text = dateFormat.format(Date(item.dateReceived))
    }
}
