package com.realityexpander.whatsupp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.realityexpander.whatsupp.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var bind: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }

    fun onClick(v: View) {
        startActivity(MainActivity.newIntent(this))
        finish()
    }


    companion object {
        fun newIntent(context: Context) = Intent(context, SignupActivity::class.java)
    }
}