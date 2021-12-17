package com.realityexpander.whatsupp.util

// Permissions
val REQUEST_PERMISSIONS_READ_CONTACTS = 10001

// Contacts Activity
const val CONTACTS_PARAM_NAME = "Contact Name"
const val CONTACTS_PARAM_PHONE = "Contact Phone"

// Conversations Activity
const val CONVERSATIONS_PARAM_CHAT_ID = "Chat id"
const val CONVERSATIONS_PARAM_PARTNER_PROFILE_IMAGE_URL = "Image url"
const val CONVERSATIONS_PARAM_PARTNER_USER_ID = "Other user id"
const val CONVERSATIONS_PARAM_CHAT_NAME = "Chat name"


///////// FIREBASE STORAGE /////////

// Images Data Store
val DATA_IMAGES = "Images"


///////// FIREBASE CLOUD FIRESTORE ///////

// Users collection
const val DATA_USERS_COLLECTION = "users" // userId & partnerUserId
const val DATA_USER_EMAIL = "email"
const val DATA_USER_USERNAME = "username"
const val DATA_USER_PHONE = "phone"
const val DATA_USER_UID = "uid"
const val DATA_USER_PROFILE_IMAGE_URL = "profileImageUrl"
const val DATA_USER_STATUS_URL = "statusUrl"
const val DATA_USER_STATUS_TIMESTAMP = "statusTimestamp"
  // subdocument: /chats
const val DATA_USER_CHATS = "userChats"  // Map of partnerUserId -> chatId

// Chats collection
const val DATA_CHATS_COLLECTION = "chats" // chatId
  // sub-document: /chatParticipants
const val DATA_CHAT_PARTICIPANTS = "chatParticipants"  // Array of userId
  // sub-collection: messages
const val DATA_CHAT_MESSAGES_COLLECTION = "messages" // Array of Message
const val DATA_CHAT_MESSAGE_TIMESTAMP = "timestamp"