package com.realityexpander.whatsupp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.realityexpander.whatsupp.databinding.ActivityStatusBinding
import com.realityexpander.whatsupp.util.StatusListItem
import com.realityexpander.whatsupp.util.loadUrl
import kotlinx.coroutines.*

const val PARAM_STATUS_ELEMENT = "element"

class StatusActivity : AppCompatActivity() /*, CoroutineScope*/ {
    private lateinit var bind: ActivityStatusBinding
    private lateinit var statusElement: StatusListItem

//    override val coroutineContext = Job() + Dispatchers.Main
//    val mainScope = MainScope()
    val timerScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(bind.root)

        if(intent.hasExtra(PARAM_STATUS_ELEMENT)) {
            statusElement = intent.getParcelableExtra(PARAM_STATUS_ELEMENT)!!
        } else {
            Toast.makeText(this, "Unable to get status", Toast.LENGTH_SHORT).show()
            finish()
        }

        bind.statusTv.text = statusElement.statusMessage
        bind.statusIv.loadUrl(statusElement.statusUrl)

        bind.progressBar.max = 100
        val errorHandler = CoroutineExceptionHandler {
            context, throwable->
        }
//        coroutineContext
//        mainScope.launch(errorHandler) {
        timerScope.launch(errorHandler) {
            withContext(Dispatchers.IO) {
                for(i in 1..100) {
                    delay(30)
                    onProgressUpdate(i)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        timerScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        timerScope.cancel()
    }

    private fun onProgressUpdate(progress: Int) {
        bind.progressBar.progress = progress
        if(progress == 100) {
            finish()
        }
    }

    companion object {
        fun getIntent(context: Context?, statusElement: StatusListItem): Intent {
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra(PARAM_STATUS_ELEMENT, statusElement)
            return intent
        }
    }
}
