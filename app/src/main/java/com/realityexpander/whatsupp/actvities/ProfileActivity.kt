package com.realityexpander.whatsupp.actvities

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityProfileBinding
import com.realityexpander.whatsupp.util.*
import java.lang.Exception


class ProfileActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUser: User? = null
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user == null) {
            // When logged out/deleted, go to the signup activity
            startActivity(SignupActivity.newIntent(this))
            finish()
        }
    }

    private lateinit var bind: ActivityProfileBinding

    // Setup photo picker (new way)
    private val resultPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                Glide.with(this)
                    .load(uri)
                    .into(bind.profileImageIv)

                storeProfileImage(uri)
            }
        }

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

        // Setup progress tap-eater
        bind.progressLayout.setOnTouchListener { _, _ ->
            true // this will block any tap events
        }

        //setup text-etry reset error messages listeners
        setOnTextChangedListener(bind.nameET, bind.nameTIL)
        setOnTextChangedListener(bind.phoneET, bind.phoneTIL)

        // photo picker for profileImage
        bind.profileImageIv.setOnClickListener {
            resultPhotoLauncher.launch(arrayOf("image/*")) // OpenDocument
        }

        populateInfo()
    }

    override fun onResume() {
        super.onResume()

        if(firebaseAuth.currentUser == null) {
            startActivity(LoginActivity.newIntent(this))
            finish()
        }
    }

    private fun populateInfo() {
        bind.progressLayout.visibility = View.VISIBLE

        firebaseDB.collection(DATA_USER_COLLECTION).document(currentUserId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                currentUser = documentSnapshot.toObject(User::class.java)

                bind.emailAddress.setText(currentUser?.email)
                bind.nameET.setText(currentUser?.username)
                bind.phoneET.setText(currentUser?.phone)
                currentUser?.profileImageUrl.let {
                    bind.profileImageIv.loadUrl(currentUser?.profileImageUrl,
                        R.drawable.default_user)
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

    fun onChangePassword(view: View) {
        bind.progressLayout.visibility = View.VISIBLE

        firebaseAuth.sendPasswordResetEmail(currentUser?.email!!)
            .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@ProfileActivity,
                        "We have sent you instructions to reset your password!",
                        Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ProfileActivity,
                        "Failed to send reset email!",
                        Toast.LENGTH_SHORT).show()
                }
                bind.progressLayout.visibility = View.INVISIBLE
            })
    }

    fun onUpdateProfile(view: View) {
        var proceed = true

        // Check for form errors
        if (bind.nameET.text.isNullOrEmpty()) {
            bind.nameTIL.error = "Name is required"
            bind.nameTIL.isErrorEnabled = true
            proceed = false
        }
        if (bind.phoneET.text.isNullOrEmpty()) {
            bind.phoneTIL.error = "Phone is required"
            bind.phoneTIL.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            val phone = bind.phoneET.text.toString()
            val name = bind.nameET.text.toString()
            val saveUser = currentUser?.copy(phone = phone, username = name)

            // Save to database
            bind.progressLayout.visibility = View.VISIBLE
            if (saveUser != null) {
                firebaseDB.collection(DATA_USER_COLLECTION)
                    .document(firebaseAuth.uid!!)
                    .set(saveUser)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@ProfileActivity,
                                "Update profile successful!",
                                Toast.LENGTH_SHORT).show()

                            currentUser = saveUser
                            finish()
                        } else {
                            Toast.makeText(this@ProfileActivity,
                                "Update profile unsuccessful! Try again later.",
                                Toast.LENGTH_LONG).show()
                        }
                        bind.progressLayout.visibility = View.INVISIBLE
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@ProfileActivity,
                            "Update profile unsuccessful! Try again later.",
                            Toast.LENGTH_LONG).show()
                        bind.progressLayout.visibility = View.INVISIBLE
                        e.printStackTrace()
                    }
            }
        }
    }

    fun onDeleteAccount(view: View) {
        // confirm delete
        val deleteAccountAction: (userId: String) -> Unit = { userId ->
            deleteUserAccount(userId)
        }
        confirmDialog(
            this@ProfileActivity,
            "Permanently Delete ${currentUser?.username}'s account?",
            currentUserId!!,
            deleteAccountAction
        )
    }

    private fun deleteUserAccount(userId: String) {
        bind.progressLayout.visibility = View.VISIBLE

        // Second delete the Firebase Database entry for this User
        fun deleteUserAccountData(userId: String) {
            firebaseDB.collection(DATA_USER_COLLECTION)
                .document(userId)
                .delete()
                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@ProfileActivity,
                            "Your account & profile are deleted!",
                            Toast.LENGTH_SHORT).show()

                        currentUser = null
                        bind.progressLayout.visibility = View.INVISIBLE
                        startActivity(Intent(this@ProfileActivity, SignupActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@ProfileActivity,
                            "Failed to delete your account!",
                            Toast.LENGTH_SHORT).show()
                        bind.progressLayout.visibility = View.INVISIBLE
                    }
                })
                .addOnFailureListener {
                    Toast.makeText(this@ProfileActivity,
                        "Failed to delete your account!",
                        Toast.LENGTH_SHORT).show()
                    bind.progressLayout.visibility = View.INVISIBLE
                }
        }

        // first delete the Firebase Auth account
        if (currentUser != null) {
            firebaseAuth.currentUser!!
                .delete()
                .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        deleteUserAccountData(currentUserId!!)
                    } else {
                        Toast.makeText(this@ProfileActivity,
                            "Failed to delete your account!",
                            Toast.LENGTH_SHORT).show()
                        bind.progressLayout.visibility = View.INVISIBLE
                    }
                })
                .addOnFailureListener {
                    Toast.makeText(this@ProfileActivity,
                        "Failed to delete your account!",
                        Toast.LENGTH_LONG).show()
                    bind.progressLayout.visibility = View.INVISIBLE
                }
        }
    }

    // To remove the error warning when user types into the fields
    private fun setOnTextChangedListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }

    fun onProfileImageAdd(view: View) {
        resultPhotoLauncher.launch(arrayOf("image/*")) // OpenDocument
    }

    // Save the profile image to the firebase Storage
    private fun storeProfileImage(profileImageUri: Uri?) {

        // show failure message
        fun onUploadFailure(e: Exception) {
            e.printStackTrace()
            Toast.makeText(this,
                "Profile Image upload failed, please try again later. ${e.localizedMessage}",
                Toast.LENGTH_LONG).show()
            bind.progressLayout.visibility = View.GONE
        }

        profileImageUri?.let {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show()
            bind.progressLayout.visibility = View.VISIBLE

            // Upload the new profile image to firebase Storage
            val profileImageStorageRef =
                firebaseStorage.child(DATA_USER_PROFILE_IMAGE_URL)
                    .child(currentUserId!!)
            profileImageStorageRef.putFile(profileImageUri)
                .addOnSuccessListener {

                    // Download the new profile image from firebase Storage
                    profileImageStorageRef.downloadUrl
                        .addOnSuccessListener { profileImageUri ->

                            // Update the users' profile in the firebase user database with the new profileImageUrl
                            val profileImageUrl = profileImageUri.toString()

                            // create the hashmap for firebaseDB update object
                            val map = HashMap<String, Any>()
                            map[DATA_USER_PROFILE_IMAGE_URL] = profileImageUrl
                            //map[DATA_USERS_UPDATED_TIMESTAMP] = System.currentTimeMillis()

                            // Update the user account info with the new uploaded profile image
                            firebaseDB.collection(DATA_USER_COLLECTION)
                                .document(currentUserId)
                                .update(map)
                                .addOnSuccessListener { _ ->
                                    // bind.profileImageIv.loadUrl(profileImageUrl, R.drawable.default_user) // we optimistically loaded the profile image after it was picked

                                    Toast.makeText(this,
                                        "Profile image update successful",
                                        Toast.LENGTH_SHORT).show()
                                    bind.progressLayout.visibility = View.GONE
                                }
                                .addOnFailureListener { e ->
                                    onUploadFailure(e)
                                }
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



