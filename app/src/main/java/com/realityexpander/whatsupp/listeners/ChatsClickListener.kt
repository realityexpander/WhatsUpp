package com.realityexpander.whatsupp.listeners

interface ChatsClickListener {
    fun onChatClicked(
        chatId: String?,
        partnerId: String?,
        partnerProfileImageUrl: String?,
        partnerUsername: String?,
    )
}