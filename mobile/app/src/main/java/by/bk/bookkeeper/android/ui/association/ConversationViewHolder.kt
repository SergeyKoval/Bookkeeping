package by.bk.bookkeeper.android.ui.association

import android.view.View
import by.bk.bookkeeper.android.databinding.ItemConversationBinding
import by.bk.bookkeeper.android.sms.Conversation
import by.bk.bookkeeper.android.ui.BaseViewHolder
import by.bk.bookkeeper.android.ui.RecyclerClick
import io.reactivex.subjects.PublishSubject

/**
 *  Created by Evgenia Grinkevich on 03, February, 2020
 **/

class ConversationViewHolder(private val clicksSubject: PublishSubject<RecyclerClick>, private val binding: ItemConversationBinding) : BaseViewHolder<Conversation>(binding.root) {

    init {
        itemView.setOnClickListener {
            clicksSubject.onNext(RecyclerClick.Row(adapterPosition))
        }
    }

    override fun setItem(item: Conversation?) {
        val sender = item?.sender ?: return
        binding.tvSnippet.text = sender.snippet
        if (item.sender.addressBookDisplayableName.isNotEmpty()) {
            binding.tvSenderName.text = sender.addressBookDisplayableName
            binding.tvPhoneNumber.text = sender.address
            binding.tvPhoneNumber.visibility = View.VISIBLE
        } else {
            binding.tvSenderName.text = sender.address
            binding.tvPhoneNumber.visibility = View.GONE
        }

    }
}
