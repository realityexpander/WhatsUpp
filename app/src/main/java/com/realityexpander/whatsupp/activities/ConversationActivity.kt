package com.realityexpander.whatsupp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
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
import com.realityexpander.whatsupp.utils.*

class ConversationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityConversationBinding
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)
    private var chatId: String? = null
    private var partnerProfileImageUrl: String? = null
    private var partnerId: String? = null
    private var partnerUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(bind.root)

        title = getApplicationName(this) + " Chat"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent != null) {
            chatId = intent.extras?.getString(CONVERSATIONS_PARAM_CHAT_ID)
            partnerProfileImageUrl = intent.extras?.getString(CONVERSATIONS_PARAM_PARTNER_PROFILE_IMAGE_URL)
            partnerUsername = intent.extras?.getString(CONVERSATIONS_PARAM_PARTNER_USERNAME)
            partnerId = intent.extras?.getString(CONVERSATIONS_PARAM_PARTNER_USER_ID)
        } else if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chat room error, chatId or userId is bad.", Toast.LENGTH_SHORT).show()
            finish()
        }

        bind.partnerProfileUsernameTv.text = partnerUsername
        bind.partnerProfileImageIv.loadUrl(partnerProfileImageUrl, R.drawable.default_user)

        bind.messagesRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        // Listen for chat messages in database
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
                            val message = change.document.toObject(Message::class.java)

                            when (change.type) {
                                DocumentChange.Type.ADDED -> {
                                    conversationAdapter.addMessage(message)
                                    bind.messagesRV.post {
                                        bind.messagesRV.smoothScrollToPosition(
                                            conversationAdapter.itemCount - 1
                                        )
                                    }
                                }
                                DocumentChange.Type.MODIFIED -> {
                                    conversationAdapter.modifyMessage(message)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    conversationAdapter.removeMessage(message)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // println("onSaveInstanceState for ProfileActivity")

        outState.apply {
            putString(CONVERSATION_ACTIVITY_CHAT_ID, chatId)
            putString(CONVERSATION_ACTIVITY_PARTNER_ID, partnerId)
            putString(CONVERSATION_ACTIVITY_PARTNER_PROFILE_USERNAME, partnerUsername)
            putString(CONVERSATION_ACTIVITY_PARTNER_PROFILE_IMAGE_URL, partnerProfileImageUrl)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for ProfileActivity")

        savedInstanceState.apply {
            chatId = getString(CONVERSATION_ACTIVITY_CHAT_ID, "")
            partnerId = getString(CONVERSATION_ACTIVITY_PARTNER_ID, "")
            partnerUsername = getString(CONVERSATION_ACTIVITY_PARTNER_PROFILE_USERNAME, "")
            partnerProfileImageUrl = getString(CONVERSATION_ACTIVITY_PARTNER_PROFILE_IMAGE_URL)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
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
                .document() // new document
                .set(message)

            // reset the send message EditText entry
            bind.messageET.setText("", TextView.BufferType.EDITABLE)
        }
    }

    companion object {
        // To navigate to this Conversation activity
        fun newIntent(
            context: Context?,
            chatId: String?,
            partnerProfileImageUrl: String?,
            partnerId: String?,
            partnerUsername: String?
        ): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(CONVERSATIONS_PARAM_CHAT_ID, chatId)
            intent.putExtra(CONVERSATIONS_PARAM_PARTNER_PROFILE_IMAGE_URL, partnerProfileImageUrl)
            intent.putExtra(CONVERSATIONS_PARAM_PARTNER_USER_ID, partnerId)
            intent.putExtra(CONVERSATIONS_PARAM_PARTNER_USERNAME, partnerUsername)
            return intent
        }
    }
}