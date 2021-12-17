package com.realityexpander.whatsupp.listener

interface ChatsClickListener {
    fun onChatClicked(
        chatId: String?,
        partnerUserId: String?,
        chatImageUrl: String?,
        chatName: String?,
    )
}