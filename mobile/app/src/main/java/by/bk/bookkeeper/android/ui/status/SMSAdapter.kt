package by.bk.bookkeeper.android.ui.status

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.getListItemDateFormat
import by.bk.bookkeeper.android.network.request.MatchedSms
import by.bk.bookkeeper.android.ui.BaseViewHolder
import kotlinx.android.synthetic.main.item_sms_body.view.tv_date_sent
import kotlinx.android.synthetic.main.item_sms_body.view.tv_sms_body
import kotlinx.android.synthetic.main.item_sms_detailed.view.*
import java.util.*

/**
 *  Created by Evgenia Grinkevich on 07, February, 2020
 **/

class PendingSMSAdapter : RecyclerView.Adapter<PendingSMSViewHolder>() {

    private var sms: List<MatchedSms> = arrayListOf()

    fun setData(sms: List<MatchedSms>) {
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

class PendingSMSViewHolder(view: View) : BaseViewHolder<MatchedSms>(view) {

    private val dateSentTextView: TextView = itemView.tv_date_sent
    private val senderTextView: TextView = itemView.tv_sms_sender
    private val smsBodyTextView: TextView = itemView.tv_sms_body
    private val accountInfoTextView: TextView = itemView.tv_account_subaccount
    private val dateFormat = getListItemDateFormat()

    override fun setItem(item: MatchedSms?) {
        item ?: return
        senderTextView.text = item.sms.senderName
        smsBodyTextView.text = item.sms.body
        accountInfoTextView.text = itemView.context.getString(R.string.msg_sms_status_account_subaccount_info,
                item.accountName, item.subAccountName)
        dateSentTextView.text = dateFormat.format(Date(item.sms.dateReceived))
    }
}
