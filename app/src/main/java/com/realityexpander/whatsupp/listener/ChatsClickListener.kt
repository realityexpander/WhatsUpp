package com.realityexpander.whatsupp.listener

interface ChatsClickListener {
    fun onChatClicked(
        chatId: String?,
        otherUserId: String?,
        chatImageUrl: String?,
        chatName: String?,
    )
}