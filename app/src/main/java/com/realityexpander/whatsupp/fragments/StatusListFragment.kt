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
import com.realityexpander.whatsupp.activities.MainActivity
import com.realityexpander.whatsupp.activities.StatusActivity
import com.realityexpander.whatsupp.adapters.StatusListAdapter
import com.realityexpander.whatsupp.databinding.FragmentStatusListBinding
import com.realityexpander.whatsupp.listener.StatusItemClickListener
import com.realityexpander.whatsupp.util.DATA_USERS_COLLECTION
import com.realityexpander.whatsupp.util.DATA_USER_CHATS
import com.realityexpander.whatsupp.util.StatusListItem
import com.realityexpander.whatsupp.util.User

/**
 * A simple [Fragment] subclass.
 * Use the [StatusListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusListFragment : Fragment(), StatusItemClickListener {

    private var _bind: FragmentStatusListBinding? = null
    private val bind: FragmentStatusListBinding
        get() = _bind!!

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())

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

    fun onVisible() {
        statusListAdapter.onRefresh()
        refreshList()
    }

    private fun refreshList() {

        bind.progressBar.visibility = View.VISIBLE

        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(userId!!)
            .get()
            .addOnSuccessListener { userDoc ->

                // If there are any chats for this user...
                if (userDoc.contains(DATA_USER_CHATS)) {
                    val partners = userDoc[DATA_USER_CHATS]

                    @Suppress("UNCHECKED_CAST")
                    for (partnerId in (partners as HashMap<String, String>).keys) {

                        firebaseDB.collection(DATA_USERS_COLLECTION)
                            .document(partnerId)
                            .get()
                            .addOnSuccessListener { partnerUserDoc ->
                                val partner = partnerUserDoc.toObject(User::class.java)

                                partner?.let {
                                    if (!partner.statusMessage.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()) {
                                        val item = StatusListItem(
                                            username = partner.username,
                                            profileImageUrl = partner.profileImageUrl,
                                            statusUrl = partner.statusUrl,
                                            statusMessage = partner.statusMessage,
                                            statusTimestamp = partner.statusTimestamp,
                                            statusDate = partner.statusDate
                                        )
                                        statusListAdapter.addItem(item)
                                    }
                                }
                            }
                    }
                    bind.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e->
                bind.progressBar.visibility = View.GONE
                Toast.makeText(activity, "Error updating status. Please try again later.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
    }

    override fun onItemClicked(statusItem: StatusListItem) {
        startActivity(StatusActivity.getIntent(context, statusItem))
    }

}