package com.realityexpander.whatsupp.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.activities.MainActivity
import com.realityexpander.whatsupp.databinding.FragmentStatusUpdateBinding
import com.realityexpander.whatsupp.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StatusUpdateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatusUpdateFragment : Fragment() {
    private var _bind: FragmentStatusUpdateBinding? = null
    private val bind: FragmentStatusUpdateBinding
        get() = _bind!!

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private var pickedImageUri: Uri? = null // uri to an image file on android device before updating status
    private var savedStatusImageUrl = "" // url in firestore db after the user updates status

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _bind = FragmentStatusUpdateBinding.inflate(inflater, container, false)
        return bind.root
    }

    @SuppressLint("ClickableViewAccessibility") // for progressLayout touch event blocker
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bind.progressLayout.setOnTouchListener { _, _ -> true }
        bind.sendStatusButton.setOnClickListener { onUpdate() }

//        bind.statusIv.loadUrl(savedStatusImageUrl, R.drawable.default_user)

        bind.statusLayout.setOnClickListener { _ ->
            if (isAdded) {
                startImagePickerActivity { imageUri ->
                    pickedImageUri = imageUri

                    // preview the status Image, only save upon user performing status update
                    bind.statusIv.loadUrl(pickedImageUri.toString(), R.drawable.default_user)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure user is logged in
        if (currentUserId.isNullOrEmpty()) {
            (activity as MainActivity).onUserNotLoggedInError()
        }
    }

    fun onVisible() {
        bind.progressLayout.visibility = View.VISIBLE

        // Populate with the current user status
        firebaseDB.collection(DATA_USERS_COLLECTION).document(currentUserId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                val currentUser = documentSnapshot.toObject(User::class.java)

                bind.statusEt.setText(currentUser?.statusMessage)
                currentUser?.statusUrl?.let { statusImageUrl ->
                    bind.statusIv.loadUrl(statusImageUrl)
                    savedStatusImageUrl = statusImageUrl
                }
                bind.progressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity,
                    "Error retrieving user data. Please try again later.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
    }

    private fun onUpdate() {
        bind.progressLayout.visibility = View.VISIBLE
        val updateMap = HashMap<String, Any>()

        // save the new status image, if there is one.
        pickedImageUri?.let {
            storeStatusImage(pickedImageUri)
            pickedImageUri = null // to prevent duplicate uploads of same file
        }

        updateMap[DATA_USER_STATUS_MESSAGE] = bind.statusEt.text.toString()
        updateMap[DATA_USER_STATUS_URL] = savedStatusImageUrl
        updateMap[DATA_USER_STATUS_DATE] = getCurrentDateString()
        updateMap[DATA_USER_STATUS_TIMESTAMP] = System.currentTimeMillis().toString()

        firebaseDB.collection(DATA_USERS_COLLECTION)
            .document(currentUserId!!)
            .update(updateMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Status updated!", Toast.LENGTH_SHORT).show()
                bind.progressLayout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                bind.progressLayout.visibility = View.GONE
                Toast.makeText(
                    activity,
                    "Error updating status. PLease try again later.",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }
    }

    private fun storeStatusImage(imageUri: Uri?) {
        if (imageUri == null) return

        Toast.makeText(activity, "Uploading Status Image...", Toast.LENGTH_SHORT).show()
        bind.progressLayout.visibility = View.VISIBLE

        fun onUploadImageFailure(e: Exception) {
            Toast.makeText(
                activity,
                "Error uploading image. Please try again later.",
                Toast.LENGTH_LONG
            ).show()
            bind.progressLayout.visibility = View.GONE
            e.printStackTrace()
        }

        // 1. Upload the file
        val filepath = firebaseStorage.child(DATA_IMAGES)
            .child("${currentUserId}_status")
        filepath.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(activity, "File uploaded!", Toast.LENGTH_SHORT).show()

                // 2. Download the statusImage file
                filepath.downloadUrl
                    .addOnSuccessListener { savedFileUri ->
                        val savedFileUrl = savedFileUri.toString()

                        // 3. Save the statusImage URL to the user record in the database
                        firebaseDB.collection(DATA_USERS_COLLECTION)
                            .document(currentUserId!!)
                            .update(DATA_USER_STATUS_URL, savedFileUrl)
                            .addOnSuccessListener {
                                savedStatusImageUrl = savedFileUrl

                                // load the newly saved image from the firebase storage
                                bind.statusIv.loadUrl(savedStatusImageUrl)

                                bind.progressLayout.visibility = View.GONE
                            }
                            .addOnFailureListener { e ->
                                onUploadImageFailure(e)
                            }
                    }
                    .addOnFailureListener { e ->
                        onUploadImageFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                onUploadImageFailure(e)
            }
    }

    // Setup image picker (must be setup before onResume/onStart)
    private var imagePickerResultCallback: (uri: Uri) -> Unit = {}
    private val imagePickerForResultLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                imagePickerResultCallback(uri)
            }
        }

    private fun startImagePickerActivity(imagePickerResultCallback: (uri: Uri) -> Unit) {
        this.imagePickerResultCallback = imagePickerResultCallback
        imagePickerForResultLauncher.launch(arrayOf("image/*")) // Launch Image Picker
    }
}