package com.realityexpander.whatsupp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.listener.StatusItemClickListener
import com.realityexpander.whatsupp.util.StatusListItem
import com.realityexpander.whatsupp.util.loadUrl

class StatusListAdapter(val statusList: ArrayList<StatusListItem>):
    RecyclerView.Adapter<StatusListAdapter.StatusListViewHolder>() {

    private var clickListener: StatusItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int) = StatusListViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_status_list, parent, false)
    )

    fun onRefresh() {
        statusList.clear()
        notifyDataSetChanged()
    }

    fun addItem(item: StatusListItem) {
        statusList.add(item)
        notifyDataSetChanged()
    }

    override fun getItemCount() = statusList.size

    override fun onBindViewHolder(holder: StatusListViewHolder, position: Int) {
        holder.bind(statusList[position], clickListener)
    }

    fun setOnItemClickListener(listener: StatusItemClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    class StatusListViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val layout = view.findViewById<RelativeLayout>(R.id.itemLayout)
        private val profileImageIv = view.findViewById<ImageView>(R.id.profileImageIv)
        private val usernameTv = view.findViewById<TextView>(R.id.usernameTv)
        private val statusMessageTv = view.findViewById<TextView>(R.id.statusMessageTv)
        private val statusDateTv = view.findViewById<TextView>(R.id.statusDateTV)

        fun bind(item: StatusListItem, listener: StatusItemClickListener?) {
            profileImageIv.loadUrl(item.profileImageUrl, R.drawable.default_user)
            usernameTv.text = item.username
            statusDateTv.text = item.statusDate
            statusMessageTv.text = item.statusMessage
            layout?.setOnClickListener {listener?.onItemClicked(item)}
        }
    }
}