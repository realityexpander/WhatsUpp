package com.realityexpander.whatsupp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.R.style.Base_Theme_MaterialComponents_Dialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityMainBinding
import com.realityexpander.whatsupp.fragments.BaseFragment
import com.realityexpander.whatsupp.fragments.ChatListFragment
import com.realityexpander.whatsupp.fragments.StatusListFragment
import com.realityexpander.whatsupp.fragments.StatusUpdateFragment
import com.realityexpander.whatsupp.interfaces.HostContextI
import com.realityexpander.whatsupp.listeners.UserNotLoggedInError
import com.realityexpander.whatsupp.utils.*


class MainActivity : AppCompatActivity(),
    UserNotLoggedInError,
    HostContextI
{
    private lateinit var bind: ActivityMainBinding
    private var sectionPagerAdapter: SectionPagerAdapter? = null
    private var chatsFragment = ChatListFragment()
    private var statusListFragment = StatusListFragment()
    private var statusUpdateFragment = StatusUpdateFragment()

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    // Fragment Identifiers
    private enum class FragmentId {
        STATUS_UPDATE,
        CHAT_LIST,
        STATUS_LIST,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setSupportActionBar(bind.toolbar)

        sectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        bind.container.adapter = sectionPagerAdapter
        bind.container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(bind.tabs))
        bind.tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(bind.container))
        resizeTabs()
        bind.tabs.getTabAt(1)?.select()
        bind.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    FragmentId.STATUS_UPDATE.ordinal -> {
                        bind.fab.hide()
                        statusUpdateFragment.onUpdateUI()
                    }
                    FragmentId.CHAT_LIST.ordinal -> {
                        bind.fab.show()
                    }
                    FragmentId.STATUS_LIST.ordinal -> {
                        bind.fab.hide()
                        statusListFragment.onUpdateUI()
                    }
                }
            }
        })


        bind.fab.setOnClickListener { view ->
            onNewChat(view)
//            Snackbar.make(view, "Replace with action", Snackbar.LENGTH_SHORT)
//                .setAction("Action", null)
//                .show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onGoToProfile()
        }

        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        println("onSaveInstanceState for MainActivity")

        outState.apply {
            putInt(MAIN_ACTIVITY_SELECTED_TAB_POSITION, bind.tabs.selectedTabPosition)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        println("onRestoreInstanceState for MainActivity")

        savedInstanceState.apply {
            val selectedTabPosition = getInt(MAIN_ACTIVITY_SELECTED_TAB_POSITION)
            sectionPagerAdapter?.selectTabLayoutItem(selectedTabPosition)
        }
    }
    // After process death recovery fragment creation, update the fragment vars
    override fun onAndroidFragmentCreated(fragment: BaseFragment) {

        // note: newBlitterFragment type is created in the fragment upon process death recovery
        when(fragment) {
            is ChatListFragment -> chatsFragment = fragment
            is StatusListFragment -> statusListFragment = fragment
            is StatusUpdateFragment -> statusUpdateFragment = fragment
        }

        //currentFragment = androidCreatedFragment

        // println("onBlitterFragmentCreated currentFragment=$currentFragment")
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    private fun onGoToProfile() {
        startActivity(ProfileActivity.newIntent(this))
    }


    inner class SectionPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            //  return PlaceHolderFragment.newIntent(position + 1)
            return when (position) {
                FragmentId.STATUS_UPDATE.ordinal -> statusUpdateFragment
                FragmentId.CHAT_LIST.ordinal -> chatsFragment
                FragmentId.STATUS_LIST.ordinal -> statusListFragment
                else -> statusListFragment
            }
        }

        override fun getCount(): Int {
            return 3
        }

        // Utility to Select the tab at "position" in tabLayout
        fun selectTabLayoutItem(position: Int) {
            bind.tabs.selectTab(bind.tabs.getTabAt(position), true)
        }
    }

    private fun resizeTabs() {
        val layout = (bind.tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onNewChat(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage("This app requires access to your contacts to start a conversation.")
                    .setPositiveButton("Ask me") { _, _ ->
                        requestContactsPermission()
                    }
                    .setNegativeButton("No") { _, _ -> }
                    .show()
            } else {
                requestContactsPermission()
            }
        } else {
            // Permission granted
            startContactPickerActivity()
        }
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            REQUEST_PERMISSIONS_READ_CONTACTS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContactPickerActivity()
                }
            }
        }
    }

    private val launchContactsActivity =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val name = data?.getStringExtra(CONTACTS_PARAM_NAME)
                val phone = data?.getStringExtra(CONTACTS_PARAM_PHONE)

                checkForNewChatUserAndStartNewChat(name, phone)
            }
        }

    private fun startContactPickerActivity() {
        val intent = Intent(this, ContactsActivity::class.java)
        launchContactsActivity.launch(intent)
    }

    private fun checkForNewChatUserAndStartNewChat(name: String?, phone: String?) {
        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {

            firebaseDB.collection(DATA_USERS_COLLECTION)
                .whereEqualTo(DATA_USER_PHONE, phone) // Unique key is the phone number for the user
                .get()
                .addOnSuccessListener { result ->
                    if (result.documents.size > 0) {
                        val partnerId = result.documents[0].id

                        if (partnerId != firebaseAuth.currentUser?.uid) {
                            // Found valid partner with phone number, so start a new chat
                            chatsFragment.newChat(partnerId)
                        } else {
                            simpleErrorMessageDialog( this@MainActivity,"Can't send messages to yourself.")
                        }
                    } else {
                        // Invite the new user via SMS
                        AlertDialog.Builder(this, Base_Theme_MaterialComponents_Dialog)
                            .setTitle("User not found")
                            .setMessage(
                                "$name does not have an account. " +
                                        "Send them an SMS to ask them to install this app."
                            )
                            .setPositiveButton("OK") { _, _ ->
                                val intent = Intent(Intent.ACTION_SENDTO)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra(
                                    "sms_body", "Hi. I'm using this new cool " +
                                            "WhatsUpp app. You should install it too so we can chat there."
                                )
                                startActivity(intent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "An error occurred. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
        }
    }

    override fun onUserNotLoggedInError() {
        Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

}

//    class PlaceHolderFragment : Fragment() {
//        private lateinit var dataBind: FragmentMainBinding
//
//        override fun onCreateView(
//            inflater: LayoutInflater,
//            container: ViewGroup?,
//            savedInstanceState: Bundle?,
//        ): View? {
//            dataBind = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
//            "Hello from section ${arguments?.getInt(ARG_SECTION_NUMBER)}".also { dataBind.sectionLabel.text = it }
//            return dataBind.root
//        }
//
//        companion object {
//            private val ARG_SECTION_NUMBER = "Section number"
//
//            fun newIntent(sectionNumber: Int): PlaceHolderFragment {
//                val fragment = PlaceHolderFragment()
//                val args = Bundle()
//                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
//                fragment.arguments = args
//
//                return fragment
//            }
//        }
//    }




























