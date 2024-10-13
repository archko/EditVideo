package com.archko.editvideo.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 */
class Utils {

    companion object {
        /**
         * 获取屏幕高度(px)
         */
        fun getScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }

        /**
         * 获取屏幕宽度(px)
         */
        fun getScreenWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        fun dip2Px(context: Context, dip: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dip * scale + 0.5f).toInt()
        }

        fun sp2px(context: Context, spValue: Float): Int {
            val fontScale: Float = context.resources.displayMetrics.scaledDensity
            return (spValue * fontScale + 0.5f).toInt()
        }

        fun pixelToDip(context: Context, px: Float): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (px / scale + 0.5f).toInt()
        }

        fun isNetworkConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
            return false
        }

        //判断WIFI网络是否可用
        fun isWifiConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                if (mWiFiNetworkInfo != null) {
                    return mWiFiNetworkInfo.isAvailable
                }
            }
            return false
        }

        //判断MOBILE网络是否可用
        fun isMobileConnected(context: Context?): Boolean {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                if (mMobileNetworkInfo != null) {
                    return mMobileNetworkInfo.isAvailable
                }
            }
            return false
        }
    }
}