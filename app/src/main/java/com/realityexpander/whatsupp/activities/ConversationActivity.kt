package com.realityexpander.whatsupp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.adapters.ConversationAdapter
import com.realityexpander.whatsupp.databinding.ActivityConversationBinding
import com.realityexpander.whatsupp.util.*

class ConversationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityConversationBinding
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(bind.root)

        chatId = intent.extras?.getString(CONVERSATIONS_PARAM_CHAT_ID)
        imageUrl = intent.extras?.getString(CONVERSATIONS_PARAM_IMAGE_URL)
        chatName = intent.extras?.getString(CONVERSATIONS_PARAM_CHAT_NAME)
        otherUserId = intent.extras?.getString(CONVERSATIONS_PARAM_PARTNER_USER_ID)
        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chat room error", Toast.LENGTH_SHORT).show()
            finish()
        }

        bind.topNameTV.text = chatName
        bind.topPhotoIV.loadUrl(imageUrl, R.drawable.default_user)

        bind.messagesRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        // Listen for database changes (ie: messages)
        firebaseDB.collection(DATA_CHATS_COLLECTION)
            .document(chatId!!)
            .collection(DATA_CHAT_MESSAGES_COLLECTION)
            .orderBy(DATA_CHAT_MESSAGE_TIMESTAMP)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    firebaseFirestoreException.printStackTrace()
                    return@addSnapshotListener
                } else {
                    querySnapshot?.let {
                        for (change in querySnapshot.documentChanges) {
                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    val message = change.document.toObject(Message::class.java)

                                    conversationAdapter.addMessage(message)
                                    bind.messagesRV.post {
                                        bind.messagesRV.smoothScrollToPosition(
                                            conversationAdapter.itemCount - 1
                                        )
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    val message = change.document.toObject(Message::class.java)
                                    conversationAdapter.modifyMessage(message)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    val message = change.document.toObject(Message::class.java)
                                    conversationAdapter.removeMessage(message)
                                }
                            }
                        }
                    }
                }
            }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSend(v: View) {
        if (!bind.messageET.text.isNullOrEmpty()) {
            val message = Message(
                fromUserId = userId,
                message = bind.messageET.text.toString(),
                timestamp = System.currentTimeMillis()
            )

            firebaseDB.collection(DATA_CHATS_COLLECTION)
                .document(chatId!!)
                .collection(DATA_CHAT_MESSAGES_COLLECTION)
                .document()
                .set(message)
            bind.messageET.setText("", TextView.BufferType.EDITABLE)
        }
    }

    companion object {
        fun newIntent(
            context: Context?,
            chatId: String?,
            imageUrl: String?,
            otherUserId: String?,
            chatName: String?
        ): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CONVERSATIONS_PARAM_CHAT_ID, chatId)
            intent.putExtra(CONVERSATIONS_PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(CONVERSATIONS_PARAM_PARTNER_USER_ID, otherUserId)
            intent.putExtra(CONVERSATIONS_PARAM_CHAT_NAME, chatName)
            return intent
        }
    }
}