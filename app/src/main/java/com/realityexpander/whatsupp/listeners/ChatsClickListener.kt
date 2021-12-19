package com.realityexpander.whatsupp.listeners

interface ChatsClickListener {
    fun onChatClicked(
        chatId: String?,
        partnerUserId: String?,
        chatImageUrl: String?,
        chatName: String?,
    )
}