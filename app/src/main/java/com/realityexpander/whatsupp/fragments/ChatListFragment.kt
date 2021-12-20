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
import com.realityexpander.whatsupp.utils.*

/**
 * A simple [Fragment] subclass.
 * Use the [ChatListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatListFragment : BaseFragment(), ChatsClickListener, UpdateUIExternally {
    private var _bind: FragmentChatsBinding? = null
    private val bind: FragmentChatsBinding
        get() = _bind!!

//    private var chatsAdapter = ChatsAdapter(arrayListOf<ChatId>())
    private var chatsAdapter = ChatsAdapter(arrayListOf<ChatIdAndUnreadCount>())
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val partnerStatusListenerSet = mutableSetOf<ListenerRegistration>()
    private val unreadChatListenerSet = mutableSetOf<ListenerRegistration>()

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

    override fun onPause() {
        super.onPause()
        removeAllUpdateListeners()
    }

    override fun onUpdateUI() {
        refreshChatsList(false)
    }

    private fun refreshChatsList(shouldRefreshUpdateListeners: Boolean = false) {
        bind.progressBar.visibility = View.VISIBLE

        // Refresh chats
        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.contains(DATA_USER_CHATS)) {
                    val partnerChatIds = userDocument[DATA_USER_CHATS]
//                    val chats = arrayListOf<ChatId>()
                    val chats = arrayListOf<ChatIdAndUnreadCount>()
                    if(shouldRefreshUpdateListeners) removeAllUpdateListeners()

                    // Collect the list of partners for this user
                    @Suppress("UNCHECKED_CAST")
                    for ((index, partnerId) in (partnerChatIds as HashMap<PartnerId, ChatId>).keys.withIndex()) {
                        if (partnerChatIds[partnerId] != null) {
                            chats.add(ChatIdAndUnreadCount(partnerChatIds[partnerId]!!))
                        }

                        // setup Listeners for changes to partners status changes
                        if (shouldRefreshUpdateListeners) {

                            // Partner Status listener
                            val partnerStatusListener =
                                firebaseDB.collection(DATA_USERS_COLLECTION)
                                    .document(partnerId)
                                    .addSnapshotListener { partnerDoc, firebaseFirestoreException ->
                                        if (firebaseFirestoreException == null && partnerDoc?.metadata?.isFromCache == false) {
                                            refreshChatsList(false)
                                        }
                                    }
                            partnerStatusListenerSet.add(partnerStatusListener)

                            // Unread Chat Count listener
                            val unreadChatListener = firebaseDB.collection(DATA_CHATS_COLLECTION)
                                .document(partnerChatIds[partnerId]!!)
                                .collection(DATA_CHAT_MESSAGES_COLLECTION)
                                .orderBy(DATA_CHAT_MESSAGE_TIMESTAMP)
                                .whereGreaterThan(DATA_CHAT_MESSAGE_TIMESTAMP, System.currentTimeMillis())
                                .addSnapshotListener { messagesDoc, firebaseFirestoreException ->
                                    if (firebaseFirestoreException == null && messagesDoc?.metadata?.isFromCache == false) {
                                        // println("num unread messages: ${messagesDoc.size()}")
                                        chats[index].unreadChatCount = messagesDoc.size()
                                        chatsAdapter.updateChats(chats)
                                    }
                                }
                            unreadChatListenerSet.add(unreadChatListener)
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
    private fun removeAllUpdateListeners() {
        for(partnerStatusListener in partnerStatusListenerSet) {
            partnerStatusListener.remove()
        }
        partnerStatusListenerSet.clear()

        for(unreadChatListener in unreadChatListenerSet) {
            unreadChatListener.remove()
        }
        unreadChatListenerSet.clear()
    }

    fun newChat(partnerId: String) {
        var isChatAlreadyStarted = false
        var user_SavedChatsMap = HashMap<PartnerId, ChatId>()

        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener first@ { userDocument ->
                val user_ChatsMap = hashMapOf<PartnerId, ChatId>()

                // Create the chat for the current userId (if it doesn't already exist)
                userDocument[DATA_USER_CHATS]?.let { user_DocumentChatMap ->
                    if (user_DocumentChatMap is HashMap<*, *>) {
                        @Suppress("UNCHECKED_CAST")
                        user_SavedChatsMap = user_DocumentChatMap as HashMap<PartnerId, ChatId>

                        // Does a chat already exist for this partnerId?
                        if (user_SavedChatsMap.containsKey(partnerId)) {
                            isChatAlreadyStarted = true
                        } else {
                            user_ChatsMap.putAll(user_SavedChatsMap)
                        }
                    }
                }

                // If Chat exists, start the ChatActivity after looking up the partnerId
                if (isChatAlreadyStarted) {
                    firebaseDB.collection(DATA_USERS_COLLECTION)
                        .document(partnerId)
                        .get()
                        .addOnSuccessListener { partnerDocument ->
                            val partner = partnerDocument.toObject(User::class.java)

                            // start the chat now
                            onChatItemClicked(
                                user_SavedChatsMap[partnerId],
                                partnerId,
                                partner?.profileImageUrl,
                                partner?.username
                            )
                            return@addOnSuccessListener
                        }
                        .addOnFailureListener { e ->
                            simpleErrorMessageDialog(this.requireContext(), "PartnerId not found.")
                            e.printStackTrace()
                        }

                    return@first
                }

                // Create the chat for the partnerId (it should not exist, we checked above!)
                firebaseDB.collection(DATA_USERS_COLLECTION)
                    .document(partnerId)
                    .get()
                    .addOnSuccessListener { partnerDocument ->
                        val partner_ChatsMap = hashMapOf<PartnerId, ChatId>()

                        // Create the chat for the partner (if it doesn't already exist)
                        partnerDocument[DATA_USER_CHATS]?.let { partner_DocumentChatsMap ->
                            if (partner_DocumentChatsMap is HashMap<*, *>) {
                                @Suppress("UNCHECKED_CAST")
                                val partner_SavedChatsMap = partner_DocumentChatsMap as HashMap<PartnerId, ChatId>
                                partner_ChatsMap.putAll(partner_SavedChatsMap)
                            }
                        }

                        // Prepare to save the new chat then update the user and partner
                        val chatParticipants = Chat(arrayListOf(userId, partnerId))
                        val newChatDocRef = firebaseDB.collection(DATA_CHATS_COLLECTION).document() // new document
                        val userDocRef = firebaseDB.collection(DATA_USERS_COLLECTION).document(userId)
                        val partnerDocRef = firebaseDB.collection(DATA_USERS_COLLECTION).document(partnerId)

                        // point both user and partner to the same chat document
                        user_ChatsMap[partnerId] = newChatDocRef.id
                        partner_ChatsMap[userId] = newChatDocRef.id

                        // Update the database for user & partner chat map at same time
                        val batch = firebaseDB.batch()
                        batch.set(newChatDocRef, chatParticipants)
                        batch.update(userDocRef, DATA_USER_CHATS, user_ChatsMap)
                        batch.update(partnerDocRef, DATA_USER_CHATS, partner_ChatsMap)
                        batch.commit()

                        // Start chat now
                        val partner = partnerDocument.toObject(User::class.java)
                        onChatItemClicked(newChatDocRef.id, partnerId, partner?.profileImageUrl, partner?.username)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }

    }

    override fun onChatItemClicked(
        chatId: String?,
        partnerId: String?,
        partnerProfileImageUrl: String?,
        partnerUsername: String?,
    ) {
        startActivity(
            ConversationActivity.newIntent(context,
            chatId,
            partnerProfileImageUrl,
            partnerId,
            partnerUsername)
        )
    }
}