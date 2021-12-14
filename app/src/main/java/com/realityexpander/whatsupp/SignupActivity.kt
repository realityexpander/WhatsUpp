package com.realityexpander.whatsupp

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
import com.realityexpander.whatsupp.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var bind: ActivitySignupBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if(user != null) {
            // When logged in, go to the main activity
            startActivity(MainActivity.newIntent(this))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignupBinding.inflate(layoutInflater)
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

    fun onGoToLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
        finish()
    }

    fun onSignup(v: View) {
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
            firebaseAuth.createUserWithEmailAndPassword(bind.emailET.text.toString(), bind.passwordET.text.toString())
                .addOnCompleteListener { task->
                    bind.progressLayout.visibility = View.INVISIBLE
                    if(!task.isSuccessful) {
                        Toast.makeText(this@SignupActivity, "Signup unsuccessful ${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e->
                    bind.progressLayout.visibility = View.INVISIBLE
                    e.printStackTrace()
                }

        }
    }

    // To cancel the error warning when user types into the fields
    private fun setOnTextChangedListener(et: EditText, til: TextInputLayout) {
        et.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                til.isErrorEnabled = false
            }
        })
    }


    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}