package com.realityexpander.whatsupp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.realityexpander.whatsupp.databinding.ActivityLoginBinding
import com.realityexpander.whatsupp.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var bind: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }

    fun onSignup(v: View) {
        startActivity(SignupActivity.newIntent(this))
        finish()
    }

    fun onLogin(v: View) {
        Toast.makeText(v.context, "Log in", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

}