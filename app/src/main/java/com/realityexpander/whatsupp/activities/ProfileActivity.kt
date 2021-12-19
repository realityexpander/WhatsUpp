package com.realityexpander.whatsupp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityProfileBinding
import com.realityexpander.whatsupp.utils.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.lang.Exception


class ProfileActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUser: User? = null
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user == null) {
            // When logged out/deleted, go to the signup activity
            startActivity(SignupActivity.newIntent(this))
            finish()
        }
    }

    private lateinit var photoPickerForResultLauncher: ActivityResultLauncher<Array<out String>>
    private var savedProfileImageUrl = ""
    private var pickedImageUri: Uri? = null

    private lateinit var bind: ActivityProfileBinding

    companion object {
        fun newIntent(context: Context) = Intent(context, ProfileActivity::class.java)
    }

    @SuppressLint("ClickableViewAccessibility") // for progress layout event eater layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(bind.root)

        title = "Profile"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup progressLayout tap event blocker
        bind.progressLayout.setOnTouchListener { _, _ ->
            true // this will block any tap events
        }

        //setup text-etry reset error messages listeners
        setOnTextChangedListener(bind.nameEt, bind.nameTIL)
        setOnTextChangedListener(bind.phoneEt, bind.phoneTIL)

        // Setup photo picker (new way)
        photoPickerForResultLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    pickedImageUri = uri
                    bind.profileImageIv.loadUrl(uri.toString(), R.drawable.default_user)
                }
            }

        // photo picker for profileImage
        bind.profileImageIv.setOnClickListener {
            photoPickerForResultLauncher.launch(arrayOf("image/*")) // OpenDocument
        }

        if (savedInstanceState == null) {
            populateInfo()
        } else {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onResume() {
        super.onResume()

        if (firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // println("onSaveInstanceState for ProfileActivity")

        outState.apply {
            putString(PROFILE_ACTIVITY_EMAIL, bind.emailAddressTv.text.toString())
            putString(PROFILE_ACTIVITY_USERNAME, bind.nameEt.text.toString())
            putString(PROFILE_ACTIVITY_PHONE_NUMBER, bind.phoneEt.text.toString())
            pickedImageUri?.let {
                putString(PROFILE_ACTIVITY_PICKED_IMAGE_URI, pickedImageUri.toString())
            }
            putString(PROFILE_ACTIVITY_SAVED_PROFILE_IMAGE_URL, savedProfileImageUrl)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for ProfileActivity")

        savedInstanceState.apply {
            bind.nameEt.setText(getString(PROFILE_ACTIVITY_USERNAME, ""))
            bind.phoneEt.setText(getString(PROFILE_ACTIVITY_PHONE_NUMBER, ""))
            bind.emailAddressTv.text = getString(PROFILE_ACTIVITY_EMAIL, "")
            pickedImageUri = getString(PROFILE_ACTIVITY_PICKED_IMAGE_URI)?.let { uriString ->
                uriString.toUri()
            }
            savedProfileImageUrl = getString(PROFILE_ACTIVITY_SAVED_PROFILE_IMAGE_URL, "")

            if (pickedImageUri != null) {
                bind.profileImageIv.loadUrl(pickedImageUri.toString())
            } else {
                bind.profileImageIv.loadUrl(savedProfileImageUrl)
            }
        }
    }

    private fun populateInfo() {
        bind.progressLayout.visibility = View.VISIBLE

        firebaseDB.collection(DATA_USERS_COLLECTION).document(currentUserId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                currentUser = documentSnapshot.toObject(User::class.java)

                bind.emailAddressTv.setText(currentUser?.email)
                bind.nameEt.setText(currentUser?.username)
                bind.phoneEt.setText(currentUser?.phone)
                currentUser?.profileImageUrl?.let { profileImageUrl ->
                    bind.profileImageIv.loadUrl(
                        profileImageUrl,
                        R.drawable.default_user
                    )
                    savedProfileImageUrl = profileImageUrl
                }
                bind.progressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun onChangePassword(view: View) {
        bind.progressLayout.visibility = View.VISIBLE

        firebaseAuth.sendPasswordResetEmail(currentUser?.email!!)
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@ProfileActivity,
                        "We have sent you instructions to reset your password!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to send reset email!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                bind.progressLayout.visibility = View.INVISIBLE
            })
    }

    @Suppress("UNUSED_PARAMETER")
    fun onUpdateProfile(view: View) {
        var proceed = true

        // Check for form errors
        if (bind.nameEt.text.isNullOrEmpty()) {
            bind.nameTIL.error = "Name is required"
            bind.nameTIL.isErrorEnabled = true
            proceed = false
        }
        if (bind.phoneEt.text.isNullOrEmpty()) {
            bind.phoneTIL.error = "Phone is required"
            bind.phoneTIL.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {

            pickedImageUri?.let {
                storeProfileImage(pickedImageUri)
                pickedImageUri = null
            }

            val username = bind.nameEt.text.toString()
            val phone = bind.phoneEt.text.toString()
            val saveUser = currentUser?.copy(
                phone = phone,
                username = username,
                profileImageUrl = savedProfileImageUrl
            )

            val updateMap = HashMap<String, Any>()
            updateMap[DATA_USER_USERNAME] = username
            updateMap[DATA_USER_PHONE] = phone
            updateMap[DATA_USER_PROFILE_IMAGE_URL] = savedProfileImageUrl

            // Save to database
            bind.progressLayout.visibility = View.VISIBLE
            firebaseDB.collection(DATA_USERS_COLLECTION)
                .document(firebaseAuth.uid!!)
                .update(updateMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Update profile successful!",
                            Toast.LENGTH_SHORT
                        ).show()

                        currentUser = saveUser
                        finish()
                    } else {
                        Toast.makeText(
                            this@ProfileActivity,
                            "Update profile unsuccessful! Try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    bind.progressLayout.visibility = View.INVISIBLE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@ProfileActivity,
                        "Update profile unsuccessful! Try again later.",
                        Toast.LENGTH_LONG
                    ).show()
                    bind.progressLayout.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
    }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onDeleteAccount(view: View) {
        confirmDialog(
            this@ProfileActivity,
            "Permanently Delete ${currentUser?.username}'s account?",
            currentUserId!!
        ) { deleteUserId ->
            deleteUserAccount(deleteUserId)
        }
    }

    private fun deleteUserAccount(deleteUserId: String) {
        bind.progressLayout.visibility = View.VISIBLE

        // Second delete the Firebase Database entry for this User
        fun deleteUserAccountData(deleteUserId: String) {
            firebaseDB.collection(DATA_USERS_COLLECTION)
                .document(deleteUserId)
                .delete()
                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        bind.progressLayout.visibility = View.INVISIBLE

                        Toast.makeText(
                            this@ProfileActivity,
                            "Your account & profile are deleted!",
                            Toast.LENGTH_SHORT
                        ).show()

                        currentUser = null
                        startActivity(Intent(this@ProfileActivity, SignupActivity::class.java))
                        finish()
                    } else {
                        bind.progressLayout.visibility = View.INVISIBLE
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed to delete your account!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
                .addOnFailureListener {
                    bind.progressLayout.visibility = View.INVISIBLE
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to delete your account!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // first delete the Firebase Auth account
        firebaseAuth.currentUser!!
            .delete()
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    deleteUserAccountData(deleteUserId)
                } else {
                    Toast.makeText(
                        this@ProfileActivity,
                        "Failed to delete your account! Try again later.",
                        Toast.LENGTH_SHORT
                    ).show()
                    bind.progressLayout.visibility = View.INVISIBLE
                }
            })
            .addOnFailureListener {
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to delete your account! Try again later.",
                    Toast.LENGTH_LONG
                ).show()
                bind.progressLayout.visibility = View.INVISIBLE
            }
    }

    // To remove the error warning when user types into the edit text fields
    private fun setOnTextChangedListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }

    @Suppress("UNUSED_PARAMETER")
    fun startProfileImagePickerActivity(view: View) {
        photoPickerForResultLauncher.launch(arrayOf("image/*")) // OpenDocument
    }

    // Save the profile image to the firebase Storage
    private fun storeProfileImage(imageUri: Uri?) {
        if (imageUri == null) return

        // show failure message
        fun onUploadFailure(e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Profile Image upload failed, please try again later. ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
            bind.progressLayout.visibility = View.GONE
        }

        imageUri.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
            bind.progressLayout.visibility = View.VISIBLE

            // Upload the new profile image to firebase Storage
            val profileImageStorageRef =
                firebaseStorage.child(DATA_USER_PROFILE_IMAGE_URL)
                    .child(currentUserId!!)
            profileImageStorageRef.putFile(imageUri)
                .addOnSuccessListener {

                    // Download the new profile image from firebase Storage
                    profileImageStorageRef.downloadUrl
                        .addOnSuccessListener { savedImageUri ->

                            // Update the users' profile in the firebase user database with the new profileImageUrl
                            savedProfileImageUrl = savedImageUri.toString()

//                            // create the hashmap for firebaseDB update object
//                            val map = HashMap<String, Any>()
//                            map[DATA_USER_PROFILE_IMAGE_URL] = profileImageUrl
//                            //map[DATA_USERS_UPDATED_TIMESTAMP] = System.currentTimeMillis()
//
//                            // Update the user account info with the new uploaded profile image
//                            firebaseDB.collection(DATA_USERS_COLLECTION)
//                                .document(currentUserId)
//                                .update(map)
//                                .addOnSuccessListener { _ ->
//                                    // bind.profileImageIv.loadUrl(profileImageUrl, R.drawable.default_user) // we optimistically loaded the profile image after it was picked
//
//                                    Toast.makeText(this,
//                                        "Profile image update successful",
//                                        Toast.LENGTH_SHORT).show()
//                                    bind.progressLayout.visibility = View.GONE
//                                }
//                                .addOnFailureListener { e ->
//                                    onUploadFailure(e)
//                                }
                        }
                        .addOnFailureListener { e ->
                            onUploadFailure(e)
                        }
                }
                .addOnFailureListener { e ->
                    onUploadFailure(e)
                }
        }
    }
}



