package by.bk.bookkeeper.android.ui.association

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.bk.bookkeeper.android.R
import by.bk.bookkeeper.android.sms.SMS


/**
 *  Created by Evgenia Grinkevich on 04, February, 2020
 **/

class SMSAdapter : RecyclerView.Adapter<SMSViewHolder>() {

    private var sms: List<SMS> = arrayListOf()

    fun setData(sms: List<SMS>) {
        this.sms = sms
        notifyDataSetChanged()
    }

    fun getItem(position: Int): SMS = sms[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMSViewHolder =
            SMSViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message_body, parent, false))

    override fun getItemCount(): Int = sms.size

    override fun onBindViewHolder(holder: SMSViewHolder, position: Int) {
        holder.setItem(sms[position])
    }
}