package com.realityexpander.whatsupp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.realityexpander.whatsupp.adapters.ContactsAdapter
import com.realityexpander.whatsupp.databinding.ActivityContactsBinding
import com.realityexpander.whatsupp.listeners.ContactsClickListener
import com.realityexpander.whatsupp.utils.CONTACTS_PARAM_NAME
import com.realityexpander.whatsupp.utils.CONTACTS_PARAM_PHONE
import com.realityexpander.whatsupp.utils.Contact
import com.realityexpander.whatsupp.utils.getApplicationName

class ContactsActivity : AppCompatActivity(), ContactsClickListener {

    private lateinit var bind: ActivityContactsBinding

    private var contactsList = ArrayList<Contact>()

    companion object {
        fun newIntent(context: Context) = Intent(context, ContactsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(bind.root)

        title = "Pick a contact to start a chat"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getContacts()
    }

    @SuppressLint("Range") // for warning on getColumnIndex
    private fun getContacts() {
        contactsList.clear()
        val newContactList = ArrayList<Contact>()

        // Get all the names and phone numbers in the user's Contact list
        val phoneContactCursor =
            contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null)
        while (phoneContactCursor!!.moveToNext()) {
            val name =
                phoneContactCursor.getString(phoneContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phoneContactCursor.getString(phoneContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            newContactList.add(Contact(name, phoneNumber))
        }
        phoneContactCursor.close() // destroy the cursor

        contactsList.addAll(newContactList)
        contactsList = ArrayList( contactsList.toSet()
            .sortedBy { contact ->  // Sort by name
                contact.name
            }
            .distinctBy { contact -> // remove duplicated names
                contact.name
            }
            .distinctBy { contact -> // remove duplicated phone #'s
                contact.phone
                    ?.replace("(", "")
                    ?.replace(")", "")
                    ?.replace(" ", "")
                    ?.replace("-", "")
            }
        )

        setupContactListRV()
    }
    private fun setupContactListRV() {
        bind.progressLayout.visibility = View.VISIBLE

        val contactsAdapter = ContactsAdapter(contactsList)
        contactsAdapter.setOnItemClickListener(this)
        bind.contactsRv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = contactsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Setup search query
        bind.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // if (contactsList.contains(query)) {
                    contactsAdapter.getFilter().filter(query)
                // } else {
                //     Toast.makeText(this@ContactsActivity, "No Match found", Toast.LENGTH_LONG).show()
                // }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                contactsAdapter.getFilter().filter(newText)
                return false
            }
        })

        bind.progressLayout.visibility = View.INVISIBLE
    }

    override fun onContactClicked(name: String?, phone: String?) {
        val intent = Intent()
        intent.putExtra(CONTACTS_PARAM_NAME, name)
        intent.putExtra(CONTACTS_PARAM_PHONE, phone)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}