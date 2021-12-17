package com.realityexpander.whatsupp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.activities.ConversationActivity
import com.realityexpander.whatsupp.adapters.ChatsAdapter
import com.realityexpander.whatsupp.databinding.FragmentChatsBinding
import com.realityexpander.whatsupp.listener.ChatsClickListener
import com.realityexpander.whatsupp.listener.FailureCallback
import com.realityexpander.whatsupp.util.Chat
import com.realityexpander.whatsupp.util.DATA_CHATS_COLLECTION
import com.realityexpander.whatsupp.util.DATA_USERS_COLLECTION
import com.realityexpander.whatsupp.util.DATA_USER_CHATS

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment(), ChatsClickListener {
    private var _bind: FragmentChatsBinding? = null
    private val bind: FragmentChatsBinding
        get() = _bind!!

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var failureCallback: FailureCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _bind = FragmentChatsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure user is logged in
        if (userId.isNullOrEmpty()) {
            failureCallback?.onUserError()
        }
    }

    fun setFailureCallbackListener(listener: FailureCallback) {
        failureCallback = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatsAdapter.setOnItemClickListener(this)
        bind.chatsRv.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Listen for any changes in the Database (like new messages)
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .addSnapshotListener { _, firebaseFirestoreException ->
                if (firebaseFirestoreException == null) {
                    refreshChats()
                }
            }
    }

    @Suppress("UNCHECKED_CAST") // for HashMap<String, String>
    private fun refreshChats() {
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.contains(DATA_USER_CHATS)) {
                    val partners = userDocument[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()

                    // Collect the list of partners for this user
                    for (partner in (partners as HashMap<String, String>).keys) {
                        if (partners[partner] != null) {
                            chats.add(partners[partner]!!)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                }
            }
    }

    @Suppress("UNCHECKED_CAST") // for HashMap<String, String>
    fun newChat(partnerId: String) {
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                val userChatPartnersMap = hashMapOf<String, String>()

                // Create the chat for this user (if it doesn't already exist)
                userDocument[DATA_USER_CHATS]?.let { userDocumentUserChats ->
                    if (userDocumentUserChats is HashMap<*, *>) {
                        val userChatsMap = userDocumentUserChats as HashMap<String, String>

                        if (userChatsMap.containsKey(partnerId)) {
                            return@addOnSuccessListener
                        } else {
                            userChatPartnersMap.putAll(userChatsMap)
                        }
                    }
                }

                // Create the chat for the partner (if it doesn't already exist)
                firebaseDB.collection(DATA_USERS_COLLECTION)
                    .document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partnerChatPartnersMap = hashMapOf<String, String>()

                        // Create the chat for the partner (if it doesn't already exist)
                        partnerDocument[DATA_USER_CHATS]?.let { partnerDocumentUserChats ->
                            if (partnerDocumentUserChats is HashMap<*, *>) {
                                val partnerChatsMap = partnerDocumentUserChats as HashMap<String, String>
                                partnerChatPartnersMap.putAll(partnerChatsMap)
                            }
                        }

                        // Prepare to save the new chat and update the user and partner
                        val chatParticipants = arrayListOf(userId, partnerId)
                        val chat = Chat(chatParticipants)
                        val chatDocRef = firebaseDB.collection(DATA_CHATS_COLLECTION).document()
                        val userDocRef = firebaseDB.collection(DATA_USERS_COLLECTION).document(userId)
                        val partnerDocRef = firebaseDB.collection(DATA_USERS_COLLECTION).document(partnerId)

                        userChatPartnersMap[partnerId] = chatDocRef.id
                        partnerChatPartnersMap[userId] = chatDocRef.id

                        // Update the database for user & partner chats at same time
                        val batch = firebaseDB.batch()
                        batch.set(chatDocRef, chat)
                        batch.update(userDocRef, DATA_USER_CHATS, userChatPartnersMap)
                        batch.update(partnerDocRef, DATA_USER_CHATS, partnerChatPartnersMap)
                        batch.commit()

                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }

    override fun onChatClicked(
        chatId: String?,
        partnerUserId: String?,
        chatImageUrl: String?,
        chatName: String?,
    ) {
        startActivity(
            ConversationActivity.newIntent(context,
            chatId,
            chatImageUrl,
            partnerUserId,
            chatName)
        )
    }
}