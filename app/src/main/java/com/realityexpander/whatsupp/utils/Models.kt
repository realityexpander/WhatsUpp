package com.realityexpander.whatsupp.utils

import android.os.Parcel
import android.os.Parcelable

typealias PartnerId = String
typealias UserId = String
typealias ChatId = String

data class User (
    val email: String? = "",
    val username: String? = "",
    val phone: String? = "",
    val phoneTrimmed: String? = "",  // number with trimmed extra characters to improve searches
    val uid: String? = "",
    val profileImageUrl: String? = "",
    val statusImageUrl: String? = "",
    val statusMessage: String? = "",
    val statusTimestamp: String = "",
    val statusDate: String = "",
    val userchats: HashMap<PartnerId, ChatId> = HashMap()
)

data class Contact(
    val name: String? = "",
    val phone: String? = ""
)

data class Chat(
    val chatParticipants: ArrayList<UserId>
)

// For ChatListFragment
data class ChatIdAndUnreadCount(
    val chatId: ChatId,
    var unreadChatCount: Int = 0
)

data class Message(
    val timestamp: Long? = 0L,
    val fromUserId: UserId? = "",
    val fromUserName: String? = "",
    val toUserId: UserId? = "",
    val toUserName: String? = "",
    val message: String? = "",
    val imageUrl: String? = ""
)

// For StatusListFragment
data class StatusListItem(
    val username: String?,
    val profileImageUrl: String?,
    val statusUrl: String?,
    val statusMessage: String?,
    val statusTimestamp: String?,
    val statusDate: String?
): Parcelable {
    constructor(parcel: Parcel) : this(
        username = parcel.readString(),
        profileImageUrl = parcel.readString(),
        statusUrl = parcel.readString(),
        statusMessage = parcel.readString(),
        statusTimestamp = parcel.readString(),
        statusDate = parcel.readString()
    ) { }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(profileImageUrl)
        parcel.writeString(statusUrl)
        parcel.writeString(statusMessage)
        parcel.writeString(statusTimestamp)
        parcel.writeString(statusDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<StatusListItem> {
        override fun createFromParcel(parcel: Parcel): StatusListItem {
            return StatusListItem(parcel)
        }

        override fun newArray(size: Int): Array<StatusListItem?> {
            return arrayOfNulls(size)
        }
    }

}