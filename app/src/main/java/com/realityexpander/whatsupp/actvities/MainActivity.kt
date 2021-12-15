package com.realityexpander.whatsupp.actvities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityMainBinding
import com.realityexpander.whatsupp.databinding.FragmentMainBinding
import com.realityexpander.whatsupp.fragments.ChatsFragment
import com.realityexpander.whatsupp.fragments.StatusFragment
import com.realityexpander.whatsupp.fragments.StatusUpdateFragment

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
        resizetabs()
        bind.tabs.getTabAt(1)?.select()
        bind.tabs.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                when(tab?.position) {
                    0 -> {bind.fab.hide()}
                    1 -> {bind.fab.show()}
                    2 -> {bind.fab.hide()}
                }
            }
        })


        bind.fab.setOnClickListener { view->
            Snackbar.make(view, "Replace with action", Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show()
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

    private fun resizetabs() {
        val layout = (bind.tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    fun onNewChat(view: View) {

    }

}