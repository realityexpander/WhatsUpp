package com.realityexpander.whatsupp.utils

import android.content.Context
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.realityexpander.whatsupp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun ImageView.loadUrl(url: String?, errorDrawable: Int = R.drawable.empty) {
    if (url.isNullOrEmpty()) return

    context?.let {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(progressDrawable(context))
            .fallback(progressDrawable(context))
            .error(errorDrawable)
            .override(1000, 750)
            .fitCenter()

        CoroutineScope(Dispatchers.Main).launch {
            Glide.with(context.applicationContext)
                .load(url)
                .thumbnail(0.5f)
                .apply(options)
                .into(this@loadUrl)
        }
    }
}

fun progressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorAccent))
        start()
    }
}


// Allows android:imageUrl to load URL images
@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.loadUrl(url)
}

fun Long?.getDateString(): String {
    var dateLong: Long? = this ?: return "unknown date"

    val sdf = SimpleDateFormat("EEE MMM dd, yyyy hh:mm a", Locale.US)
    val resultDate = Date(dateLong!!)

    return sdf.format(resultDate).lowercase(Locale.US)
}

fun getCurrentDateString(): String {
    val df = DateFormat.getDateInstance()
    return df.format(Date())
}

fun simpleErrorMessageDialog(context: Context, errorMessage: String) {
    AlertDialog.Builder(
        context,
        com.google.android.material.R.style.Base_Theme_MaterialComponents_Dialog
    )
        .setTitle(errorMessage)
        .setPositiveButton("OK") { _, _ -> }
        .show()
}

fun getApplicationName(context: Context): String? {
    val applicationInfo = context.applicationInfo
    val stringId = applicationInfo.labelRes

    return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString()
        else context.getString(stringId)
}

fun String.trimUnnecessaryPhoneCharacters(): String {
    return this.replace("(", "")
        .replace(")", "")
        .replace(" ", "")
        .replace("-", "")
        .replace("+", "")
}