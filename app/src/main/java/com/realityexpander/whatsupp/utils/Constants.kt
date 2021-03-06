package com.realityexpander.whatsupp.utils

// Permissions
val REQUEST_PERMISSIONS_READ_CONTACTS = 10001

// Contacts Activity
const val CONTACTS_PARAM_NAME = "Contact Name"
const val CONTACTS_PARAM_PHONE = "Contact Phone"

// Conversations Activity
const val CONVERSATIONS_PARAM_CHAT_ID = "Chat id"
const val CONVERSATIONS_PARAM_PARTNER_PROFILE_IMAGE_URL = "Image url"
const val CONVERSATIONS_PARAM_PARTNER_USER_ID = "Other user id"
const val CONVERSATIONS_PARAM_PARTNER_USERNAME = "Chat name"


///////// FIREBASE STORAGE /////////

// Images Data Store
const val DATA_IMAGES = "Images"


///////// FIREBASE CLOUD FIRESTORE ///////

// Users collection (Models.kt: User)
const val DATA_USERS_COLLECTION = "users" // userId & partnerUserId
const val DATA_USER_EMAIL = "email"
const val DATA_USER_USERNAME = "username"
const val DATA_USER_PHONE = "phone"
const val DATA_USER_PHONE_TRIMMED = "phoneTrimmed"
const val DATA_USER_UID = "uid"
const val DATA_USER_PROFILE_IMAGE_URL = "profileImageUrl"
const val DATA_USER_STATUS_URL = "statusImageUrl"
const val DATA_USER_STATUS_MESSAGE = "statusMessage"
const val DATA_USER_STATUS_TIMESTAMP = "statusTimestamp"
const val DATA_USER_STATUS_DATE = "statusDate"
  // sub-document: /chats
const val DATA_USER_CHATS = "userchats"  // Map of partnerUserId -> chatId

// Chats collection (Models.kt: Chat)
const val DATA_CHATS_COLLECTION = "chats" // chatId
  // sub-document: /chatParticipants
const val DATA_CHAT_PARTICIPANTS = "chatParticipants"  // Array of userId
  // sub-collection: messages (Models.kt: Message)
const val DATA_CHAT_MESSAGES_COLLECTION = "messages" // Array of Message
const val DATA_CHAT_MESSAGE_TIMESTAMP = "timestamp"


///////// SavedInstanceState /////////
const val MAIN_ACTIVITY_SELECTED_TAB_POSITION = "mainActivity_selectedTabPosition"

const val PROFILE_ACTIVITY_USERNAME = "profileActivity_username"
const val PROFILE_ACTIVITY_PHONE_NUMBER = "profileActivity_phoneNumber"
const val PROFILE_ACTIVITY_EMAIL = "profileActivity_email"
const val PROFILE_ACTIVITY_PICKED_IMAGE_URI = "profileActivity_pickedImageUri"
const val PROFILE_ACTIVITY_SAVED_PROFILE_IMAGE_URL = "profileActivity_savedProfileImageUrl"

const val SIGNUP_ACTIVITY_EMAIL = "signupActivity_emailAddress"
const val SIGNUP_ACTIVITY_USERNAME = "signupActivity_username"
const val SIGNUP_ACTIVITY_PHONE_NUMBER = "signupActivity_phone"
const val SIGNUP_ACTIVITY_PASSWORD = "signupActivity_password"

const val LOGIN_ACTIVITY_EMAIL = "loginActivity_emailAddress"

const val STATUS_ACTIVITY_STATUS_MESSAGE = "statusActivity_statusMessage"
const val STATUS_ACTIVITY_STATUS_DATE = "statusActivity_statusDate"
const val STATUS_ACTIVITY_STATUS_IMAGE_URL = "statusActivity_statusImageUrl"

const val CONVERSATION_ACTIVITY_CHAT_ID = "conversationActivity_chatId"
const val CONVERSATION_ACTIVITY_PARTNER_ID = "conversationActivity_partnerId"
const val CONVERSATION_ACTIVITY_PARTNER_PROFILE_USERNAME = "conversationActivity_profileUsername"
const val CONVERSATION_ACTIVITY_PARTNER_PROFILE_IMAGE_URL = "conversationActivity_profileImageUrl"