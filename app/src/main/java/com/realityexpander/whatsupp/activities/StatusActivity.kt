package com.realityexpander.whatsupp.activities

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.core.ActivityScope
import com.realityexpander.whatsupp.databinding.ActivityStatusBinding
import com.realityexpander.whatsupp.listener.ProgressListener
import com.realityexpander.whatsupp.util.StatusListItem
import com.realityexpander.whatsupp.util.loadUrl
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

const val PARAM_STATUS_ELEMENT = "element"

class StatusActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var bind: ActivityStatusBinding
    private lateinit var statusElement: StatusListItem

    override val coroutineContext = Job() + Dispatchers.Main

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
//        TimerTask(this).execute("")

        val errorHandler = CoroutineExceptionHandler {
            context, throwable->
        }
        launch(errorHandler) {
            withContext(Dispatchers.IO) {
                for(i in 1..100) {
                    delay(30)
                    onProgressUpdate(i)
                }
            }
        }

    }

    private fun onProgressUpdate(progress: Int) {
        bind.progressBar.progress = progress
        if(progress == 100) {
            finish()
        }
    }

//    private class TimerTask(val listener: ProgressListener): AsyncTask<String, Int, Any>() {
//        override fun doInBackground(vararg params: String?) {
//            var i = 0
//            val sleep = 30L
//            while (i < 100) {
//                i++
//                publishProgress(i)
//                Thread.sleep(sleep)
//            }
//        }
//
//        override fun onProgressUpdate(vararg values: Int?) {
//            if(values[0] != null) {
//                listener.onProgressUpdate(values[0]!!)
//            }
//        }
//    }

    companion object {
        fun getIntent(context: Context?, statusElement: StatusListItem): Intent {
            val intent = Intent(context, StatusActivity::class.java)
            intent.putExtra(PARAM_STATUS_ELEMENT, statusElement)
            return intent
        }
    }
}
