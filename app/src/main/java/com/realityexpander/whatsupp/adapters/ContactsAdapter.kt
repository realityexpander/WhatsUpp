package com.realityexpander.whatsupp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.listener.ContactsClickListener
import com.realityexpander.whatsupp.util.Contact

class ContactsAdapter(val contacts: ArrayList<Contact>):
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var clickListener: ContactsClickListener? = null

    class ContactsViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private var layout = view.findViewById<LinearLayout>(R.id.contactLayout)
        private var nameTv = view.findViewById<TextView>(R.id.contactNameTv)
        private var phoneTv = view.findViewById<TextView>(R.id.contactPhoneNumberTv)

        fun bind(contact: Contact, listener: ContactsClickListener?) {
            nameTv.text = contact.name
            phoneTv.text = contact.phone
            layout.setOnClickListener { listener?.onContactClicked(contact.name, contact.phone) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder = ContactsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
    )

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contacts[position], clickListener)
    }

    fun setOnItemClickListener(listener: ContactsClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = contacts.size


}