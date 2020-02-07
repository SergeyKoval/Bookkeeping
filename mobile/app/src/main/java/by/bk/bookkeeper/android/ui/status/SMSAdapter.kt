package by.bk.bookkeeper.android.ui.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.sms.SMS
import by.bk.bookkeeper.android.ui.BaseViewHolder
import kotlinx.android.synthetic.main.item_sms_body.view.tv_date_sent
import kotlinx.android.synthetic.main.item_sms_body.view.tv_sms_body
import kotlinx.android.synthetic.main.item_sms_detailed.view.*
import java.util.*

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/

class PendingSMSAdapter : RecyclerView.Adapter<PendingSMSViewHolder>() {

    private var sms: List<SMS> = arrayListOf()

    fun setData(sms: List<SMS>) {
        this.sms = sms
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingSMSViewHolder =
            PendingSMSViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_sms_detailed, parent, false))

    override fun getItemCount(): Int = sms.size

    override fun onBindViewHolder(holder: PendingSMSViewHolder, position: Int) {
        holder.setItem(sms[position])
    }
}

class PendingSMSViewHolder(view: View) : BaseViewHolder<SMS>(view) {

    private val smsBodyTextView: TextView = itemView.tv_sms_body
    private val senderTextView: TextView = itemView.tv_sms_sender
    private val dateSentTextView: TextView = itemView.tv_date_sent
    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: SMS?) {
        item ?: return
        smsBodyTextView.text = item.body
        senderTextView.text = item.senderName
        dateSentTextView.text = dateFormat.format(Date(item.dateReceived))
    }
}
