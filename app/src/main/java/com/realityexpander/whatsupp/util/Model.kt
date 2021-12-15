package com.realityexpander.whatsupp.util

data class User (
    val email: String? = "",
    val username: String? = "",
    val phone: String? = "",
    val uid: String? = "",
    val profileImageUrl: String? = "",
    val statusUrl: String? = "",
    val statusTime: String = ""
)