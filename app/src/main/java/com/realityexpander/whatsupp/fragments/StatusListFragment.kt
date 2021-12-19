package com.realityexpander.whatsupp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.realityexpander.whatsupp.activities.MainActivity
import com.realityexpander.whatsupp.activities.StatusActivity
import com.realityexpander.whatsupp.adapters.StatusListAdapter
import com.realityexpander.whatsupp.databinding.FragmentStatusListBinding
import com.realityexpander.whatsupp.interfaces.UpdateUIExternally
import com.realityexpander.whatsupp.listeners.StatusItemClickListener
import com.realityexpander.whatsupp.utils.*

/**
 * A simple [Fragment] subclass.
 * Use the [StatusListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusListFragment: BaseFragment(), StatusItemClickListener, UpdateUIExternally {

    private var _bind: FragmentStatusListBinding? = null
    private val bind: FragmentStatusListBinding
        get() = _bind!!

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())
    private val partnerStatusListenerSet = mutableSetOf<ListenerRegistration>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _bind = FragmentStatusListBinding.inflate(inflater, container, false)
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
            hostContextI?.onAndroidFragmentCreated(this@StatusListFragment)

            // not needed yet
            // onViewStateRestored(savedInstanceState)
        }

        statusListAdapter.setOnItemClickListener(this)
        bind.statusListRV.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@StatusListFragment.context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeAllPartnerStatusListeners()
    }

    override fun onUpdateUI() {
        statusListAdapter.onClearList() // Clear out the old statuses
        refreshPartnersStatusList(true)
    }


    // Refresh Partner status list and maybe update the list of partners
    private fun refreshPartnersStatusList(shouldUpdatePartnerListeners: Boolean) {

        bind.progressBar.visibility = View.VISIBLE

        fun addPartnerToStatusList(partnerId: String) {
            firebaseDB.collection(DATA_USERS_COLLECTION)
                .document(partnerId)
                .get()
                .addOnSuccessListener { partnerUserDoc ->
                    val partner = partnerUserDoc.toObject(User::class.java)

                    partner?.let {
                        // Show a status only if there is a status message or image
                        if (!partner.statusMessage.isNullOrEmpty() || !partner.statusImageUrl.isNullOrEmpty()) {
                            val item = StatusListItem(
                                username = partner.username,
                                profileImageUrl = partner.profileImageUrl,
                                statusUrl = partner.statusImageUrl,
                                statusMessage = partner.statusMessage,
                                statusTimestamp = partner.statusTimestamp,
                                statusDate = partner.statusDate
                            )
                            statusListAdapter.addItem(item)
                        }

                        // setup Listeners for changes to partners status changes
                        if (shouldUpdatePartnerListeners) {
                            val partnerStatusListener =
                                firebaseDB.collection(DATA_USERS_COLLECTION)
                                    .document(partnerId)
                                    .addSnapshotListener { partnerDoc, firebaseFirestoreException ->
                                        if (firebaseFirestoreException == null && partnerDoc?.metadata?.isFromCache == false) {
                                            statusListAdapter.onClearList() // Clear out the old statuses
                                            refreshPartnersStatusList(false)
                                        }
                                    }
                            partnerStatusListenerSet.add(partnerStatusListener)
                        }
                    }
                }
        }

        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDoc ->

                // If there are any partner chats for this userId...
                if (userDoc.contains(DATA_USER_CHATS)) {
                    val partnerIds = userDoc[DATA_USER_CHATS]
                    if(shouldUpdatePartnerListeners) removeAllPartnerStatusListeners()

                    @Suppress("UNCHECKED_CAST")
                    for (partnerId in (partnerIds as HashMap<PartnerId, ChatId>).keys) {

                        addPartnerToStatusList(partnerId)
                    }
                }
                bind.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                bind.progressBar.visibility = View.GONE
                Toast.makeText(activity, "Error updating status. Please try again later.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }
    private fun removeAllPartnerStatusListeners() {
        for(partnerStatusListener in partnerStatusListenerSet) {
            partnerStatusListener.remove()
        }
        partnerStatusListenerSet.clear()
    }

    override fun onItemClicked(statusItem: StatusListItem) {
        startActivity(StatusActivity.getIntent(context, statusItem))
    }

}