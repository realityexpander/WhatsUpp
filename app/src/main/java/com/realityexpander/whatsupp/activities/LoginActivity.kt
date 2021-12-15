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

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if(user != null) {
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

        //setup text-etry reset error messages listeners
        setOnTextChangedListener(bind.emailET, bind.emailTIL)
        setOnTextChangedListener(bind.passwordET, bind.passwordTIL)

        // setup "progress indicator" event prevention
        bind.progressLayout.setOnTouchListener { v, evt -> true /* do nothing */  }

    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    fun onGoToSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    fun onLogin(v: View) {
        var proceed = true

        if(bind.emailET.text.isNullOrEmpty()) {
            bind.emailTIL.error = "Email is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if(bind.passwordET.text.isNullOrEmpty()) {
            bind.passwordTIL.error = "Password is required"
            bind.emailTIL.isErrorEnabled = true
            proceed = false
        }
        if(proceed) {
            bind.progressLayout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(bind.emailET.text.toString(), bind.passwordET.text.toString())
                .addOnCompleteListener { task->
                    bind.progressLayout.visibility = View.INVISIBLE
                    if(!task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "Login unsuccessful ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e->
                    bind.progressLayout.visibility = View.INVISIBLE
                    e.printStackTrace()
                }

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

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

}