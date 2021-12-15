package com.realityexpander.whatsupp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import com.realityexpander.whatsupp.databinding.ActivityContactsBinding
import com.realityexpander.whatsupp.util.Contact

class ContactsActivity : AppCompatActivity() {

    private lateinit var bind: ActivityContactsBinding

    private val contactsList = ArrayList<Contact>()

    companion object {
        fun newIntent(context: Context) = Intent(context, ContactsActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(bind.root)

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
    }
}