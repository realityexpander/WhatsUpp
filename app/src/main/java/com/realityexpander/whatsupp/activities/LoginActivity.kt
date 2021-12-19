package com.realityexpander.whatsupp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.realityexpander.whatsupp.databinding.ActivityLoginBinding
import com.realityexpander.whatsupp.utils.*

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            // When logged in, go to the main activity
            bind.progressLayout.visibility = View.VISIBLE
            startActivity(MainActivity.newIntent(this))
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility") // the listener intentionally does not respond to touches
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(bind.root)

        //setup reset text-error message listeners
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
        // println("onSaveInstanceState for LoginActivity")

        outState.apply {
            putString(LOGIN_ACTIVITY_EMAIL, bind.emailEt.text.toString())
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for LoginActivity")

        savedInstanceState.apply {
            bind.emailEt.setText(getString(LOGIN_ACTIVITY_EMAIL, ""))
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onGoToSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    @Suppress("UNUSED_PARAMETER")
    fun onLogin(v: View) {
        var proceed = true

        if (bind.emailEt.text.isNullOrEmpty()) {
            bind.emailTIL.error = "Email is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (bind.passwordEt.text.isNullOrEmpty()) {
            bind.passwordTIL.error = "Password is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if (proceed) {
            bind.progressLayout.visibility = View.VISIBLE

            firebaseAuth.signInWithEmailAndPassword(
                bind.emailEt.text.toString(),
                bind.passwordEt.text.toString()
            )
                .addOnCompleteListener { task ->
                    bind.progressLayout.visibility = View.INVISIBLE
                    if (!task.isSuccessful) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Login unsuccessful ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    bind.progressLayout.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
        }
    }

    // To remove the error warning when user types into the fields again
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
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

}