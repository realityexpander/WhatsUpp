package com.realityexpander.whatsupp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityMainBinding
import com.realityexpander.whatsupp.fragments.ChatsFragment
import com.realityexpander.whatsupp.fragments.StatusFragment
import com.realityexpander.whatsupp.fragments.StatusUpdateFragment
import com.realityexpander.whatsupp.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private var sectionPagerAdapter: SectionPagerAdapter? = null
    private val chatsFragment = ChatsFragment()
    private val statusFragment = StatusFragment()
    private val statusUpdateFragment = StatusUpdateFragment()

    private val firebaseAuth = FirebaseAuth.getInstance()

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
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
        bind.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {bind.fab.hide()}
                    1 -> {bind.fab.show()}
                    2 -> {bind.fab.hide()}
                }
            }
        })


        bind.fab.setOnClickListener { view->
            onNewChat(view)
//            Snackbar.make(view, "Replace with action", Snackbar.LENGTH_SHORT)
//                .setAction("Action", null)
//                .show()
        }
    }

    override fun onResume() {
        super.onResume()

        if(firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when(item.itemId) {
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onGoToProfile()
        }

        return true
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    private fun onGoToProfile() {
        startActivity(ProfileActivity.newIntent(this))
    }


    inner class SectionPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            //  return PlaceHolderFragment.newIntent(position + 1)
            return when(position) {
                0 -> statusUpdateFragment
                1 -> chatsFragment
                2 -> statusFragment
                else -> statusFragment
            }
        }

        override fun getCount(): Int {
            return 3
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

    private fun resizeTabs() {
        val layout = (bind.tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    private fun onNewChat(view: View) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage("This app requires access to your contacts to start a conversation.")
                    .setPositiveButton("Ask me") { dialog, which ->
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
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_PERMISSIONS_READ_CONTACTS)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            REQUEST_PERMISSIONS_READ_CONTACTS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startContactPickerActivity()
                }
            }
        }
    }
    private val launchContactsActivity = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            println("${data?.getStringExtra(CONTACTS_PARAM_NAME)}, " +
                    "${data?.getStringExtra(CONTACTS_PARAM_PHONE)}")
        }
    }
    private fun startContactPickerActivity() {
        val intent = Intent(this, ContactsActivity::class.java)
        launchContactsActivity.launch(intent)
    }

}




























