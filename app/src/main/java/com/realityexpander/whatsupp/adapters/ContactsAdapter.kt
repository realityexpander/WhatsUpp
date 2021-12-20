package com.realityexpander.whatsupp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.listeners.ContactsClickListener
import com.realityexpander.whatsupp.utils.Contact
import com.realityexpander.whatsupp.utils.trimUnnecessaryPhoneCharacters
import java.util.*
import kotlin.collections.ArrayList


class ContactsAdapter(val contactList: ArrayList<Contact>):
    RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    private var contactListFiltered = contactList.clone() as ArrayList<Contact>
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactsViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
    )

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bind(contactListFiltered[position], clickListener)
    }

    fun setOnItemClickListener(listener: ContactsClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = contactListFiltered.size

    fun getFilter(): Filter {
        return object : Filter() {
            protected override fun performFiltering(charSequence: CharSequence): FilterResults? {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    contactListFiltered = contactList
                } else {
                    val filteredList = ArrayList<Contact>()
                    for (row in contactList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.name!!.lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                            || (row.phone?.trimUnnecessaryPhoneCharacters()
                                ?.contains(charSequence) == true)
                        ) {
                            filteredList.add(row)
                        }
                    }
                    contactListFiltered = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = contactListFiltered
                return filterResults
            }

            protected override fun publishResults(
                charSequence: CharSequence?,
                filterResults: FilterResults
            ) {
                @Suppress("UNCHECKED_CAST")
                contactListFiltered = filterResults.values as ArrayList<Contact>

                // refresh the list with filtered data
                notifyDataSetChanged()
            }
        }
    }
}