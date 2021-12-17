package com.realityexpander.whatsupp.util

data class User (
    val email: String? = "",
    val username: String? = "",
    val phone: String? = "",
    val uid: String? = "",
    val profileImageUrl: String? = "",
    val statusUrl: String? = "",
    val statusTimestamp: String = ""
)

data class Contact(
    val name: String? = "",
    val phone: String? = ""
)

data class Chat(
    val chatParticipants: ArrayList<String>
)

data class Message(
    val timestamp: Long? = 0L,
    val fromId: String? = "",
    val fromName: String? = "",
    val toId: String? = "",
    val toName: String? = "",
    val message: String? = "",
    val imageUrl: String? = ""
)
