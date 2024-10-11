package com.archko.editvideo.ui.activity

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.Window
import android.view.WindowManager
import androidx.media3.exoplayer.ExoPlayer
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import kotlin.math.abs

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
        const val TOUCH_MOVE_VERTICAL_LEFT = 4
        const val TOUCH_MOVE_VERTICAL_RIGHT = 5
        const val TOUCH_MOVE_HORIZONTAL = 6
    }

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f

    private var touchTime = 0L

    private var touchAction = -1

    private var mExoPlayer: ExoPlayer? = null
    private var delegateTouchListener: DelegateTouchListener? = null

    private var halfScreenWidth = 1080 / 2
    private var seekChanged = 0L
    private var touchSlop = 2
    var isLock = false
        get() = field //默认实现方式，可省略
        set(value) { //默认实现方式，可省略
            field = value //value是setter()方法参数值，field是属性本身
        }

    fun toggleLock() {
        isLock = !isLock
    }

    init {
        halfScreenWidth = activity.getScreenWidth() / 2
        touchSlop = ViewConfiguration.getTouchSlop() / 4
        if (touchSlop < 2) {
            touchSlop = 2
        }
    }

    /**
     * 屏幕旋转后需要重新设置
     */
    fun updateScreenWidth(width: Int) {
        halfScreenWidth = width / 2
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

    private fun volumeUp() {
        audioManager?.run {
            val vol = getStreamVolume(AudioManager.STREAM_MUSIC)
            setStreamVolume(AudioManager.STREAM_MUSIC, vol + 1, 0)
        }
        /*audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_PLAY_SOUND
        )*/
    }

    private fun volumeDown() {
        audioManager?.run {
            val vol = getStreamVolume(AudioManager.STREAM_MUSIC)
            setStreamVolume(AudioManager.STREAM_MUSIC, vol - 1, 0)
        }
        /*audioManager?.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_PLAY_SOUND
        )*/
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
        Log.d(TAG, "View TOUCH_LONG_PRESS")
        mExoPlayer?.setPlaybackSpeed(3f)
        touchAction = TOUCH_LONG_PRESS
        delegateTouchListener?.speed()
    }

    private val mLongPressBackRunnable: Runnable = Runnable { mExoPlayer?.setPlaybackSpeed(1f) }

    /*@SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "View ACTION_DOWN:$isLock")
                seekChanged = 0
                touchAction = TOUCH_DOWN
                touchTime = SystemClock.uptimeMillis()
                mLastMotionX = x
                mLastMotionY = y
                if (isLock) {
                    return true
                }

                handler.removeCallbacks(mLongPressBackRunnable)
                handler.postDelayed(
                    mLongPressFastRunnable,
                    ViewConfiguration.getLongPressTimeout().toLong()
                )
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isLock) {
                    return true
                }
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
                    //如果已经是长按了,不作处理
                    return true
                } else {
                    handler.removeCallbacks(mLongPressFastRunnable)
                    handler.removeCallbacks(mLongPressBackRunnable)

                    //如果已经是左右滑动的,就继续之前的,如果是垂直的也是继续之前的,否则先置为TOUCH_MOVE_INIT
                    if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                        seek(xChanged)
                    } else if (touchAction == TOUCH_MOVE_VERTICAL_LEFT) {
                        updateBrightness(yChanged)
                    } else if (touchAction == TOUCH_MOVE_VERTICAL_RIGHT) {
                        if (abs(yChanged) > 1) {
                            updateVolume(yChanged)
                        }
                    } else if (touchAction == TOUCH_MOVE_INIT) {
                        touchAction =
                            if (coef > 1) { //上下滑动
                                if (x < halfScreenWidth) {
                                    TOUCH_MOVE_VERTICAL_LEFT
                                } else {
                                    TOUCH_MOVE_VERTICAL_RIGHT
                                }
                            } else {    //左右滑动
                                TOUCH_MOVE_HORIZONTAL
                            }

                        //处理相同的滑动效果,否则会出现一会进度,一会亮度一会声音
                        if (touchAction == TOUCH_MOVE_VERTICAL_LEFT) {
                            updateBrightness(yChanged)
                        } else if (touchAction == TOUCH_MOVE_VERTICAL_RIGHT) {
                            updateVolume(xChanged)
                        } else if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                            seek(xChanged)
                        }
                    } else {
                        //刚进入移动,先判断是否移动的距离大于1,如果移动距离不够,防抖动,就不处理.
                        if (abs(xChanged) >= touchSlop || abs(yChanged) >= touchSlop) {
                            touchAction = TOUCH_MOVE_INIT
                        }
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                val delta = SystemClock.uptimeMillis() - touchTime
                Log.d(TAG, "View ACTION_UP.delta:$delta,action:$touchAction")
                handler.removeCallbacks(mLongPressFastRunnable)
                if (touchAction == TOUCH_LONG_PRESS) {
                    handler.post(mLongPressBackRunnable)
                }

                if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                    Log.d(TAG, "View ACTION_UP seek end:$seekChanged")
                    delegateTouchListener?.seekEnd(seekChanged)
                } else if (touchAction == TOUCH_MOVE_INIT || touchAction == TOUCH_DOWN) {
                    Log.d(TAG, "View ACTION_UP,click")
                    delegateTouchListener?.run {
                        this.click()
                    }
                } else {
                    Log.d(TAG, "View ACTION_UP long click")
                    delegateTouchListener?.hideTip()
                }
                touchAction = TOUCH_IDLE
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                delegateTouchListener?.hideTip()
                handler.removeCallbacks(mLongPressFastRunnable)
                if (touchAction == TOUCH_LONG_PRESS) {
                    handler.post(mLongPressBackRunnable)
                }
            }
        }

        return false
    }*/

    private var firstScroll = true
    private var gestureListener: GestureDetector.SimpleOnGestureListener =
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                firstScroll = true // 设定是触摸屏幕后第一次scroll的标志
                Log.i(TAG, "onDown")
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                Log.i(TAG, "onDoubleTapEvent")

                firstScroll = false // 第一次scroll执行完成，修改标志

                return super.onDoubleTapEvent(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Log.d(TAG, "View ACTION_UP,click")
                delegateTouchListener?.run {
                    this.click()
                }
                return super.onSingleTapConfirmed(e)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Log.i(TAG, "onScroll:$firstScroll, distanceX:$distanceX, distanceY:$distanceY")
                val mOldX = e1!!.x
                if (firstScroll) { // 以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
                    // 横向的距离变化大则调整进度，纵向的变化大则调整音量
                    if (abs(distanceX.toDouble()) >= abs(distanceY.toDouble())) {
                        touchAction = TOUCH_MOVE_HORIZONTAL
                    } else {
                        if (mOldX > halfScreenWidth) { // 音量
                            touchAction = TOUCH_MOVE_VERTICAL_RIGHT
                        } else if (mOldX < halfScreenWidth) { // 亮度
                            touchAction = TOUCH_MOVE_VERTICAL_LEFT
                        }
                    }
                }

                // 如果每次触摸屏幕后第一次scroll是调节进度，那之后的scroll事件都处理音量进度，直到离开屏幕执行下一次操作
                if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                    if (abs(distanceX.toDouble()) > abs(distanceY.toDouble())) { // 横向移动大于纵向移动
                        seek(-distanceX)
                    }
                } else if (touchAction == TOUCH_MOVE_VERTICAL_RIGHT) {
                    if (abs(distanceY.toDouble()) > abs(distanceX.toDouble())) { // 纵向移动大于横向移动
                        updateVolume(distanceY)
                    }
                } else if (touchAction == TOUCH_MOVE_VERTICAL_LEFT) {
                    updateBrightness(distanceY)
                }

                firstScroll = false // 第一次scroll执行完成，修改标志
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                touchAction = TOUCH_LONG_PRESS
                handler.removeCallbacks(mLongPressBackRunnable)
                handler.postDelayed(
                    mLongPressFastRunnable,
                    ViewConfiguration.getLongPressTimeout().toLong()
                )
            }
        }
    private var gestureDetector: GestureDetector = GestureDetector(
        activity,
        gestureListener
    )
    //gestureDetector.setIsLongpressEnabled(true);

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            handler.removeCallbacks(mLongPressFastRunnable)
            if (touchAction == TOUCH_LONG_PRESS) {
                handler.post(mLongPressBackRunnable)
            }

            if (touchAction == TOUCH_MOVE_HORIZONTAL) {
                Log.d(TAG, "View ACTION_UP seek end:$seekChanged")
                delegateTouchListener?.seekEnd(seekChanged)
            } else if (touchAction == TOUCH_MOVE_INIT || touchAction == TOUCH_DOWN) {
                Log.d(TAG, "View ACTION_UP,click")
                delegateTouchListener?.run {
                    this.click()
                }
            } else {
                Log.d(TAG, "View ACTION_UP long click")
                delegateTouchListener?.hideTip()
            }
            touchAction = TOUCH_IDLE
        }
        return gestureDetector.onTouchEvent(event)
    }

    private fun seek(xChanged: Float) {
        if (xChanged > 0) {
            seekChanged += 1000
        } else {
            seekChanged -= 1000
        }
        delegateTouchListener?.seek(seekChanged)
    }

    private fun updateVolume(yChanged: Float) {
        if (yChanged == 0.0f) {
            return
        }
        val last = getSystemVolume()
        if (yChanged > 0) {
            volumeUp()
        } else {
            volumeDown()
        }
        val current = getSystemVolume()
        Log.d(TAG, "View setVolume.last:$last, current:$current, yChanged:$yChanged")
        delegateTouchListener?.volumeChange(last, current)
    }

    private fun updateBrightness(yChanged: Float) {
        if (yChanged == 0.0f) {
            return
        }
        //Log.d(TAG, "View updateBrightness:$brightness, yChanged:$yChanged")
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
    }

    interface DelegateTouchListener {

        fun click()
        fun speed()
        fun volumeChange(last: Int, current: Int)
        fun brightnessChange(current: Double)
        fun seek(change: Long)
        fun seekEnd(changed: Long)
        fun hideTip()
    }
}