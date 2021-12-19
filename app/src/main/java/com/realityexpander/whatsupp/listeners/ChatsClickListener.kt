package com.realityexpander.whatsupp.listeners

interface ChatsClickListener {
    fun onChatItemClicked(
        chatId: String?,
        partnerId: String?,
        partnerProfileImageUrl: String?,
        partnerUsername: String?,
    )
}