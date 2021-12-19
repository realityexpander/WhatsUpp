package com.realityexpander.whatsupp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.realityexpander.whatsupp.databinding.ActivityStatusBinding
import com.realityexpander.whatsupp.utils.*
import kotlinx.coroutines.*

const val PARAM_STATUS_ITEM = "status item"
const val SHOW_STATUS_TIME_MS = 3000 // total time to show status in ms

class StatusActivity : AppCompatActivity() {
    private lateinit var bind: ActivityStatusBinding
    private lateinit var statusItem: StatusListItem

    private val timerScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityStatusBinding.inflate(layoutInflater)
        setContentView(bind.root)

        // Get Status Item Object
        if(intent.hasExtra(PARAM_STATUS_ITEM)) {
            statusItem = intent.getParcelableExtra(PARAM_STATUS_ITEM)!!
        } else {
            Toast.makeText(this, "Unable to get status", Toast.LENGTH_SHORT).show()
            finish()
        }

        if (savedInstanceState == null) {
            bind.statusTv.text = statusItem.statusMessage
            bind.statusDateTv.text = statusItem.statusDate
            bind.statusIv.loadUrl(statusItem.statusUrl)
        }

        bind.progressBar.max = 100
        val errorHandler = CoroutineExceptionHandler { _, throwable->
            simpleErrorMessageDialog( this@StatusActivity,"An Error occurred: ${throwable.localizedMessage}")
        }
        timerScope.launch(errorHandler) {
            withContext(Dispatchers.IO) {
                for(i in 1..100) {
                    delay(SHOW_STATUS_TIME_MS / 100L)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // println("onSaveInstanceState for ProfileActivity")

        outState.apply {
            putString(STATUS_ACTIVITY_STATUS_MESSAGE, bind.statusTv.text.toString())
            putString(STATUS_ACTIVITY_STATUS_DATE, bind.statusDateTv.text.toString())
            putString(STATUS_ACTIVITY_STATUS_IMAGE_URL, statusItem.statusUrl)
        }
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // println("onRestoreInstanceState for ProfileActivity")

        savedInstanceState.apply {
            bind.statusTv.text = getString(STATUS_ACTIVITY_STATUS_MESSAGE, "")
            bind.statusDateTv.text = getString(STATUS_ACTIVITY_STATUS_DATE, "")
            bind.statusIv.loadUrl(getString(STATUS_ACTIVITY_STATUS_IMAGE_URL, ""))
        }
    }

    private fun onProgressUpdate(progress: Int) {
        if(progress > 100) throw IllegalStateException("Cannot set progress over 100.")
        if(progress < 0) throw IllegalStateException("Cannot set progress under 0.")

        bind.progressBar.progress = progress
        if(progress == 100) {
            finish()
        }
    }

    companion object {
        fun getIntent(context: Context?, statusItem: StatusListItem): Intent {
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra(PARAM_STATUS_ITEM, statusItem)
            return intent
        }
    }
}
