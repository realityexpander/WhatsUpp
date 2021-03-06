package com.realityexpander.whatsupp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.listeners.ChatsClickListener
import com.realityexpander.whatsupp.utils.*


//class ChatsAdapter(val chats: ArrayList<ChatId>):
class ChatsAdapter(val chats: ArrayList<ChatIdAndUnreadCount>):
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var clickListener: ChatsClickListener? = null

    class ChatsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val firebaseDB = FirebaseFirestore.getInstance()
        private val userId = FirebaseAuth.getInstance().currentUser?.uid
        private var layout = view.findViewById<RelativeLayout>(R.id.chatLayout)
        private var chatProfileImageIv = view.findViewById<ImageView>(R.id.chatProfileImageIv)
        private var chatNameTv = view.findViewById<TextView>(R.id.chatTv)
        private var chatUnreadCountTv = view.findViewById<TextView>(R.id.chatUnreadCountTv)
        private var unreadCountCv = view.findViewById<MaterialCardView>(R.id.unreadCountCv)
        private var progressLayout = view.findViewById<LinearLayout>(R.id.progressLayout)
        private var partnerId: String? = null
        private var chatPartnerProfileImageUrl: String? = null
        private var chatPartnerUsername: String? = null

        @SuppressLint("ClickableViewAccessibility") // for progressLayout event trapper
        fun bind(chat: ChatIdAndUnreadCount, listener: ChatsClickListener?) {
            progressLayout.visibility = View.VISIBLE
            progressLayout.setOnTouchListener { _, _ -> true /* do nothing */  }

            // Fill in partnerId data for this chatId
            firebaseDB.collection(DATA_CHATS_COLLECTION)
                .document(chat.chatId)
                .get()
                .addOnSuccessListener { chatDocument ->
                    val chatParticipants = chatDocument[DATA_CHAT_PARTICIPANTS]

                    // In the chat participants list, find the partnerId
                    chatParticipants?.let {
                        @Suppress("UNCHECKED_CAST")
                        for(participant in chatParticipants as ArrayList<String>) {
                            if(participant != userId) { // don't include the current user in the list
                                partnerId = participant

                                // Look up the partner info
                                firebaseDB.collection(DATA_USERS_COLLECTION)
                                    .document(partnerId!!)
                                    .get()
                                    .addOnSuccessListener { documentSnap ->
                                        val user = documentSnap.toObject(User::class.java)
                                        chatPartnerProfileImageUrl = user?.profileImageUrl
                                        chatPartnerUsername = user?.username
                                        chatNameTv.text = user?.username
                                        chatProfileImageIv.loadUrl(user?.profileImageUrl, R.drawable.default_user)
                                        progressLayout.visibility = View.GONE
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                        progressLayout.visibility = View.GONE
                                    }
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    progressLayout.visibility = View.GONE
                }

            if(chat.unreadChatCount > 0) {
                unreadCountCv.visibility = View.VISIBLE
                chatUnreadCountTv.text = chat.unreadChatCount.toString()
            } else {
                unreadCountCv.visibility = View.INVISIBLE
            }

            // Navigate the the Chat for this partner
            layout.setOnClickListener {
                listener?.onChatItemClicked(chat.chatId, partnerId, chatPartnerProfileImageUrl, chatPartnerUsername)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
    )

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position], clickListener)
    }

    fun updateChats(updatedChats: ArrayList<ChatIdAndUnreadCount>) {
        chats.clear()
        chats.addAll(updatedChats)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: ChatsClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = chats.size
}