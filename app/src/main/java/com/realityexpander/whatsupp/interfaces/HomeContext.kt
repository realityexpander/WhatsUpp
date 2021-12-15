package com.realityexpander.whatsupp.interfaces

import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.util.User

// HomeContextI shared with the fragments
interface HomeContextI {
    val firebaseDB: FirebaseFirestore
    val currentUserId: String?
    var currentUser: User?
}