package com.archko.editvideo.ui.activity

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity

/**
 * @author: archko 2018/7/22 :13:03
 */
class SensorHelper(private val activity: ComponentActivity) {

    private var prevOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    fun setOrientation(orientation: Int) {
        if (orientation != prevOrientation) {
            activity.requestedOrientation = orientation
            prevOrientation = orientation
        }
    }

    fun getOrientation(): Int {
        return activity.requestedOrientation
    }

}