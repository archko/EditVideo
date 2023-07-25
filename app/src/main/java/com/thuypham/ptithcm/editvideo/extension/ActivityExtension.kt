package com.thuypham.ptithcm.editvideo.extension

import android.app.Activity
import android.content.Intent
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File


fun AppCompatActivity.shareDataToOtherApp(content: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, content)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}


fun Activity.shareImageToOtherApp(imagePath: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        val file = File(imagePath)
        val uri =
            FileProvider.getUriForFile(this@shareImageToOtherApp, application.packageName, file);
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/jpeg"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Fragment.shareImageToOtherApp(imagePath: String) {
    requireActivity().shareImageToOtherApp(imagePath)
}

fun Activity.getScreenWidth(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}