package com.realityexpander.whatsupp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.realityexpander.whatsupp.databinding.ActivitySignupBinding
import com.realityexpander.whatsupp.utils.*

class SignupActivity : AppCompatActivity() {

    private lateinit var bind: ActivitySignupBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            // When logged in, go to the main activity
            startActivity(MainActivity.newIntent(this))
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility") // for progressLayout event touch blocker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(bind.root)

        //setup text-entry reset-error-messages listeners
        setOnTextChangedListener(bind.nameEt, bind.nameTIL)
        setOnTextChangedListener(bind.phoneEt, bind.phoneTIL)
        setOnTextChangedListener(bind.emailEt, bind.emailTIL)
        setOnTextChangedListener(bind.passwordEt, bind.passwordTIL)

        // setup "progress indicator" event prevention
        bind.progressLayout.setOnTouchListener { _, _ -> true /* do nothing */ }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // println("onSaveInstanceState for ProfileActivity")

        outState.apply {
            putString(SIGNUP_ACTIVITY_EMAIL, bind.emailEt.text.toString())
            putString(SIGNUP_ACTIVITY_USERNAME, bind.nameEt.text.toString())
            putString(SIGNUP_ACTIVITY_PHONE_NUMBER, bind.phoneEt.text.toString())
            putString(SIGNUP_ACTIVITY_PASSWORD, bind.passwordEt.text.toString())
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for ProfileActivity")

        savedInstanceState.apply {
            bind.emailEt.setText(getString(SIGNUP_ACTIVITY_EMAIL, ""))
            bind.nameEt.setText(getString(SIGNUP_ACTIVITY_USERNAME, ""))
            bind.phoneEt.setText(getString(SIGNUP_ACTIVITY_PHONE_NUMBER, ""))
            bind.passwordEt.setText(getString(SIGNUP_ACTIVITY_PASSWORD, ""))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onGoToLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onSignup(v: View) {
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
        if (bind.emailEt.text.isNullOrEmpty()) {
            bind.emailTIL.error = "Email is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (bind.passwordEt.text.isNullOrEmpty()) {
            bind.passwordTIL.error = "Password is required"
            bind.passwordTIL.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            bind.progressLayout.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(bind.emailEt.text.toString(),
                bind.passwordEt.text.toString())
                .addOnCompleteListener { task ->
                    bind.progressLayout.visibility = View.INVISIBLE
                    if (!task.isSuccessful) {
                        Toast.makeText(this@SignupActivity,
                            "Signup unsuccessful ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT).show()
                    } else if (firebaseAuth.uid != null) {
                        val email = bind.emailEt.text.toString()
                        val phone = bind.phoneEt.text.toString()
                        val name = bind.nameEt.text.toString()
                        val user = User(
                            email = email,
                            username = name,
                            phone = phone,
                            uid = firebaseAuth.uid,
                            "",
                            statusMessage = "Hello, I'm new.",
                            statusTimestamp = System.currentTimeMillis().toString())

                        // Save to database
                        firebaseDB.collection(DATA_USERS_COLLECTION)
                            .document(firebaseAuth.uid!!)
                            .set(user)
                    }

                }
                .addOnFailureListener { e ->
                    bind.progressLayout.visibility = View.INVISIBLE
                    e.printStackTrace()
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

    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}