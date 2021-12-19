package com.realityexpander.whatsupp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.utils.Message

const val MESSAGE_CURRENT_USER = 1
const val MESSAGE_PARTNER_USER = 2

class ConversationAdapter(private var messages: ArrayList<Message>, val userId: String?): RecyclerView.Adapter<ConversationAdapter.MessagesViewHolder>() {

    fun addMessage(message: Message) {
        messages.add(message)
        notifyDataSetChanged()
    }

    fun modifyMessage(message: Message) {
        messages = messages.filter {
            it.timestamp != message.timestamp
        } as ArrayList<Message>
        messages.add(message)
        notifyDataSetChanged()
    }

    fun removeMessage(message: Message) {
        messages = messages.filter {
            it.timestamp != message.timestamp
        } as ArrayList<Message>
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): MessagesViewHolder {
        if (type == MESSAGE_CURRENT_USER) {
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_current_user_message, parent, false))
        } else {
            return MessagesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_other_user_message, parent, false))
        }
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemViewType(position: Int): Int {
        if(messages[position].fromUserId.equals(userId)) {
            return MESSAGE_CURRENT_USER
        } else {
            return MESSAGE_PARTNER_USER
        }
    }



    class MessagesViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        fun bind(message: Message) {
            view.findViewById<TextView>(R.id.messageTV).text = message.message
        }
    }
}