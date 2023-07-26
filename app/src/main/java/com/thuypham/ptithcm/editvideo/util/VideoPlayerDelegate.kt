package com.thuypham.ptithcm.editvideo.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.Window
import android.view.WindowManager
import com.google.android.exoplayer2.ExoPlayer
import kotlin.math.abs
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth

/**
 *
 */
class VideoPlayerDelegate(private var activity: Activity) : View.OnTouchListener {
    companion object {

        const val TAG = "VideoPlayerDelegate"

        /**
         * 如果已经是长按状态,滑动这些就无效
         * 如果不是长按状态,左右滑动优先,一旦发生左右滑动,状态设为2或3,先把长按取消,然后判断左边还是右边滑动
         * up状态时,取消所有的滑动与长按
         */
        const val TOUCH_IDLE = 0
        const val TOUCH_DOWN = 1
        const val TOUCH_LONG_PRESS = 2
        const val TOUCH_MOVE_INIT = 3
        const val TOUCH_MOVE_VERTICAL = 4
        const val TOUCH_MOVE_HORIZONTAL = 5
    }

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f

    private var touchTime = 0L

    private var touchAction = -1

    private var mExoPlayer: ExoPlayer? = null
    private var delegateTouchListener: DelegateTouchListener? = null

    private var halfScreenWidth = 1080 / 2

    init {
        halfScreenWidth = activity.getScreenWidth() / 2
    }

    fun setExoPlayer(mExoPlayer: ExoPlayer?) {
        this.mExoPlayer = mExoPlayer
    }

    fun setDelegateTouchListener(delegateTouchListener: DelegateTouchListener?) {
        this.delegateTouchListener = delegateTouchListener
    }

    private val handler = Handler(Looper.getMainLooper())

    private fun getSystemVolume(): Int {
        val max: Float = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)!!.toFloat()
        return (audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)!! / max * 100).toInt()
    }

    private fun setVolume(volume: Int) {
        val max: Int = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
        val step = 100 / max
        val current = getSystemVolume()
        val progress = current * step
        Log.d(TAG, "View setVolume.max:$max, volume:$volume current:$current, progress:$progress")
        if (volume > progress) {
            volumeUp()
        } else if (volume < progress) {
            volumeDown()
        }
    }

    private fun volumeUp() {
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    private fun volumeDown() {
        audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    private var audioManager: AudioManager? = null
        get() {
            if (null == field) {
                field = activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            }
            return field
        }

    private fun setBrightness(brightness: Double) {
        val window: Window? = activity.window
        val lp: WindowManager.LayoutParams? = window?.attributes
        if (lp != null) {
            lp.screenBrightness = brightness.toFloat()
            window.attributes = lp
        }
    }

    private val brightness: Float
        get() {
            val window: Window? = activity.window
            val lp: WindowManager.LayoutParams? = window?.attributes
            //println("getBrightness:" + lp.screenBrightness)
            if (lp != null) {
                return lp.screenBrightness
            }

            return 0f
        }

    fun resetBrightness() {
        setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE.toDouble())
    }

    //长按的runnable
    private val mLongPressFastRunnable: Runnable = Runnable {
        mExoPlayer?.setPlaybackSpeed(3f)
        touchAction = TOUCH_LONG_PRESS
    }

    private val mLongPressBackRunnable: Runnable = Runnable { mExoPlayer?.setPlaybackSpeed(1f) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "View ACTION_DOWN")
                touchAction = TOUCH_DOWN
                touchTime = SystemClock.uptimeMillis()
                mLastMotionX = x
                mLastMotionY = y
                handler.removeCallbacks(mLongPressBackRunnable)
                handler.postDelayed(
                    mLongPressFastRunnable,
                    ViewConfiguration.getLongPressTimeout().toLong()
                )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val xChanged = if (mLastMotionY != -1f) {
                    x - mLastMotionX //值大于0,是从左向右
                } else {
                    0f
                }
                val yChanged = if (mLastMotionY != -1f) {
                    mLastMotionY - y
                } else {
                    0f
                }

                mLastMotionX = x
                mLastMotionY = y

                Log.d(TAG, "View ACTION_MOVE:$touchAction, xChanged:$xChanged, yChanged:$yChanged")
                val coef = abs(yChanged / xChanged)
                if (touchAction == TOUCH_LONG_PRESS) {
                    //如果已经是长按了,移动取消
                    return true
                } else {
                    handler.removeCallbacks(mLongPressFastRunnable)
                    handler.removeCallbacks(mLongPressBackRunnable)

                    //如果已经是左右滑动的,就继续,如果是垂直的也是继续,否则先置为TOUCH_MOVE_INIT
                    if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                        seek(xChanged)
                    } else if (touchAction == TOUCH_MOVE_VERTICAL) {
                        updateVolumeOrBrightness(x, yChanged)
                    } else if (touchAction == TOUCH_MOVE_INIT) {
                        touchAction =
                            if (coef > 1) { //上下滑动
                                TOUCH_MOVE_VERTICAL
                            } else {    //左右滑动
                                TOUCH_MOVE_HORIZONTAL
                            }

                        //刚进入移动,先判断是否移动的距离大于1,如果移动距离不够,防抖动,就不处理.
                        if (abs(xChanged) >= 1 || abs(yChanged) >= 1
                        ) {
                            //只有与上次相同的滑动才是,否则会出现一会进度,一会亮度一会声音
                            if (touchAction == TOUCH_MOVE_VERTICAL) {
                                updateVolumeOrBrightness(x, yChanged)
                            } else if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                                seek(xChanged)
                            }
                        }
                    } else {
                        touchAction = TOUCH_MOVE_INIT
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val delta = SystemClock.uptimeMillis() - touchTime
                Log.d(TAG, "View ACTION_UP.delta:$delta,action:$touchAction")
                handler.removeCallbacks(mLongPressFastRunnable)
                handler.post(mLongPressBackRunnable)

                if (touchAction == TOUCH_MOVE_INIT || touchAction == TOUCH_DOWN) {
                    Log.d(TAG, "View ACTION_UP,click")
                    delegateTouchListener?.run {
                        this.click()
                    }
                } else {
                    Log.d(TAG, "View ACTION_UP long click")
                }
                touchAction = TOUCH_IDLE
                delegateTouchListener?.hideTip()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                delegateTouchListener?.hideTip()
            }
        }

        return false
    }

    private fun seek(xChanged: Float) {
        delegateTouchListener?.seek(xChanged)
    }

    private fun updateVolumeOrBrightness(x: Float, yChanged: Float) {
        //Log.d(TAG, "View volumeOrSeek:$action, xChanged:$xChanged, yChanged:$yChanged")
        if (x < halfScreenWidth) {
            //brightness
            val currentBright = brightness
            var target = if (yChanged > 0) {
                currentBright + 0.01
            } else {
                currentBright - 0.01
            }

            if (target > 1) {
                target = 1.0
            } else if (target < 0) {
                target = 0.0
            }

            setBrightness(target)
            delegateTouchListener?.brightnessChange(target)
        } else {
            //volume
            val last = getSystemVolume()
            if (yChanged > 0) {
                //setVolume(last + 1)
                volumeUp()
            } else {
                //setVolume(last - 1)
                volumeDown()
            }
            val current = getSystemVolume()
            delegateTouchListener?.volumeChange(last, current)
        }
    }

    interface DelegateTouchListener {

        fun click()
        fun volumeChange(last: Int, current: Int)
        fun brightnessChange(current: Double)
        fun seek(change: Float)
        fun hideTip()
    }
}
