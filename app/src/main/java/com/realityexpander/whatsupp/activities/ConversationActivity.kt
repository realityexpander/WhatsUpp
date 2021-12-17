package com.realityexpander.whatsupp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import com.realityexpander.whatsupp.databinding.ActivityConversationBinding
import com.realityexpander.whatsupp.databinding.ActivityLoginBinding

class ConversationActivity : AppCompatActivity() {
    private lateinit var bind: ActivityConversationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(bind.root)
    }
}