package com.realityexpander.whatsupp.actvities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityMainBinding
import com.realityexpander.whatsupp.databinding.FragmentMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private var sectionPagerAdapter: SectionPagerAdapter? = null

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setSupportActionBar(bind.toolbar)
        sectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        bind.container.adapter = sectionPagerAdapter

        bind.fab.setOnClickListener { view->
            Snackbar.make(view, "Replace with action", Snackbar.LENGTH_SHORT)
                .setAction("Action", null)
                .show()
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
            return PlaceHolderFragment.newIntent(position + 1)
        }

        override fun getCount(): Int {
            return 3
        }
    }


    class PlaceHolderFragment : Fragment() {
        private lateinit var dataBind: FragmentMainBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            dataBind = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
            "Hello from section ${arguments?.getInt(ARG_SECTION_NUMBER)}".also { dataBind.sectionLabel.text = it }
            return dataBind.root
        }

        companion object {
            private val ARG_SECTION_NUMBER = "Section number"

            fun newIntent(sectionNumber: Int): PlaceHolderFragment {
                val fragment = PlaceHolderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args

                return fragment
            }
        }
    }



    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}