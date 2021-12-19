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
import com.google.firebase.firestore.ListenerRegistration
import com.realityexpander.whatsupp.activities.ConversationActivity
import com.realityexpander.whatsupp.activities.MainActivity
import com.realityexpander.whatsupp.adapters.ChatsAdapter
import com.realityexpander.whatsupp.databinding.FragmentChatsBinding
import com.realityexpander.whatsupp.interfaces.UpdateUIExternally
import com.realityexpander.whatsupp.listeners.ChatsClickListener
import com.realityexpander.whatsupp.utils.Chat
import com.realityexpander.whatsupp.utils.DATA_CHATS_COLLECTION
import com.realityexpander.whatsupp.utils.DATA_USERS_COLLECTION
import com.realityexpander.whatsupp.utils.DATA_USER_CHATS

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : BaseFragment(), ChatsClickListener, UpdateUIExternally {
    private var _bind: FragmentChatsBinding? = null
    private val bind: FragmentChatsBinding
        get() = _bind!!

    private var chatsAdapter = ChatsAdapter(arrayListOf())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val partnerStatusListenerSet = mutableSetOf<ListenerRegistration>()

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
            (activity as MainActivity).onUserNotLoggedInError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.apply {
            // After process death, pass this System-created fragment to HostContext
            hostContextI?.onAndroidFragmentCreated(this@ChatListFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }

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
            .addSnapshotListener { userDoc, firebaseFirestoreException ->
                if (firebaseFirestoreException == null && userDoc?.metadata?.isFromCache == false) {
                    onUpdateUI()
                }
            }
    }

    override fun onResume() {
        super.onResume()

        refreshChatsList(true)
    }

    override fun onUpdateUI() {
        refreshChatsList(false)
    }

    private fun refreshChatsList(shouldUpdatePartnerListeners: Boolean = false) {
        bind.progressBar.visibility = View.VISIBLE

        // Refresh chats
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.contains(DATA_USER_CHATS)) {
                    val partners = userDocument[DATA_USER_CHATS]
                    val chats = arrayListOf<String>()
                    if(shouldUpdatePartnerListeners) removeAllPartnerStatusListeners()

                    // Collect the list of partners for this user
                    @Suppress("UNCHECKED_CAST")
                    for (partnerId in (partners as HashMap<String, String>).keys) {
                        if (partners[partnerId] != null) {
                            chats.add(partners[partnerId]!!)
                        }

                        // setup Listeners for changes to partners status changes
                        if (shouldUpdatePartnerListeners) {
                            val partnerStatusListener =
                                firebaseDB.collection(DATA_USERS_COLLECTION)
                                    .document(partnerId)
                                    .addSnapshotListener { partnerDoc, firebaseFirestoreException ->
                                        if (firebaseFirestoreException == null && partnerDoc?.metadata?.isFromCache == false) {
                                            refreshChatsList(false)
                                        }
                                    }
                            partnerStatusListenerSet.add(partnerStatusListener)
                        }
                    }
                    chatsAdapter.updateChats(chats)
                    bind.progressBar.visibility = View.INVISIBLE
                }
            }
            .addOnFailureListener {
                bind.progressBar.visibility = View.INVISIBLE
            }
    }
    private fun removeAllPartnerStatusListeners() {
        for(partnerStatusListener in partnerStatusListenerSet) {
            partnerStatusListener.remove()
        }
        partnerStatusListenerSet.clear()
    }

    fun newChat(partnerId: String) {
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                val userChatPartnersMap = hashMapOf<String, String>()

                // Create the chat for this user (if it doesn't already exist)
                userDocument[DATA_USER_CHATS]?.let { userDocumentUserChats ->
                    if (userDocumentUserChats is HashMap<*, *>) {
                        @Suppress("UNCHECKED_CAST")
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
                                @Suppress("UNCHECKED_CAST")
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