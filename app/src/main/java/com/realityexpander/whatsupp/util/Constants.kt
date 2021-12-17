package com.realityexpander.whatsupp.util

// Permissions
val REQUEST_PERMISSIONS_READ_CONTACTS = 10001

// Contacts List
const val CONTACTS_PARAM_NAME = "Contact Name"
const val CONTACTS_PARAM_PHONE = "Contact Phone"

// Users
const val DATA_USERS_COLLECTION = "users"
const val DATA_USER_EMAIL = "email"
const val DATA_USER_USERNAME = "username"
const val DATA_USER_PHONE = "phone"
const val DATA_USER_UID = "uid"
const val DATA_USER_PROFILE_IMAGE_URL = "profileImageUrl"
const val DATA_USER_STATUS_URL = "statusUrl"
const val DATA_USER_STATUS_TIMESTAMP = "statusTimestamp"
  // subdocument: /chats
const val DATA_USER_CHATS = "userChats"

// Images
val DATA_IMAGES = "Images"

// Chats
const val DATA_CHATS_COLLECTION = "chats"
  // subdocument: /chatParticipants
const val DATA_CHAT_PARTICIPANTS = "chatParticipants"
  // subdocument: /messages
const val DATA_CHAT_MESSAGES = "messages"
const val DATA_CHAT_MESSAGE_TIMESTAMP = "timestamp"