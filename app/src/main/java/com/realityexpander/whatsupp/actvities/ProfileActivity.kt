package com.realityexpander.whatsupp.actvities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.R
import com.realityexpander.whatsupp.databinding.ActivityProfileBinding
import com.realityexpander.whatsupp.util.DATA_USER_DATABASE
import com.realityexpander.whatsupp.util.User
import com.realityexpander.whatsupp.util.confirmDialog
import com.realityexpander.whatsupp.util.loadUrl


class ProfileActivity : AppCompatActivity() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var currentUser: User? = null

    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if(user == null) {
            // When logged out/deleted, go to the signup activity
            startActivity(SignupActivity.newIntent(this))
            finish()
        }
    }

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

        // Setup progress tap-eater
        bind.progressLayout.setOnTouchListener { _, _ ->
            true // this will block any tap events
        }

        //setup text-etry reset error messages listeners
        setOnTextChangedListener(bind.nameET, bind.nameTIL)
        setOnTextChangedListener(bind.phoneET, bind.phoneTIL)

        populateInfo()
    }

    private fun populateInfo() {
        bind.progressLayout.visibility = View.VISIBLE

        firebaseDB.collection(DATA_USER_DATABASE).document(currentUserId!!).get()
            .addOnSuccessListener { documentSnapshot ->
                currentUser = documentSnapshot.toObject(User::class.java)

                bind.emailAddress.setText(currentUser?.email)
                bind.nameET.setText(currentUser?.username)
                bind.phoneET.setText(currentUser?.phone)
                currentUser?.profileImageUrl.let {
                    bind.profileImageView.loadUrl(currentUser?.profileImageUrl, R.drawable.default_user)
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
        if(bind.nameET.text.isNullOrEmpty()) {
            bind.nameTIL.error = "Name is required"
            bind.nameTIL.isErrorEnabled = true
            proceed = false
        }
        if(bind.phoneET.text.isNullOrEmpty()) {
            bind.phoneTIL.error = "Phone is required"
            bind.phoneTIL.isErrorEnabled = true
            proceed = false
        }

        if(proceed) {
            val phone = bind.phoneET.text.toString()
            val name = bind.nameET.text.toString()
            val saveUser = currentUser?.copy(phone = phone, username = name)

            // Save to database
            bind.progressLayout.visibility = View.VISIBLE
            if (saveUser != null) {
                firebaseDB.collection(DATA_USER_DATABASE)
                    .document(firebaseAuth.uid!!)
                    .set(saveUser)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@ProfileActivity,
                                "Update profile successful ${task.exception?.localizedMessage}",
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
        val deleteAccountAction: () -> Unit = {
            deleteCurrentUserAccount()
        }
        confirmDialog(
            this@ProfileActivity,
            "Permanently Delete ${currentUser?.username}'s account?",
            deleteAccountAction )
    }

    private fun deleteCurrentUserAccount() {
        bind.progressLayout.visibility = View.VISIBLE
        if (currentUser != null) {
            firebaseAuth.currentUser?.delete()
                ?.addOnCompleteListener(OnCompleteListener<Void?> { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@ProfileActivity,
                            "Your account & profile are deleted!!",
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
        }
    }

    // To remove the error warning when user types into the fields
    private fun setOnTextChangedListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }
}