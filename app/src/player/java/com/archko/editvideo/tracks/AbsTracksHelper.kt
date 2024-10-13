package com.archko.editvideo.tracks

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.content.res.AppCompatResources
import androidx.media3.common.C
import com.archko.editvideo.utils.Utils
import com.thuypham.ptithcm.editvideo.R
import timber.log.Timber

/**
 * @author: archko 2023/8/18 :17:36
 */
abstract class AbsTracksHelper {

    companion object {

        //原画的参数
        const val ORIGIN_RESOLUTION = "&caps=demax&ffmode=3"
    }

    var speedHolder: SpeedHolder? = null

    var audioWindow: PopupWindow? = null
    var textWindow: PopupWindow? = null
    var metaWindow: PopupWindow? = null
    var speedWindow: PopupWindow? = null
    var resolutionWindow: PopupWindow? = null

    open fun showPopupWindow(popupWindow: PopupWindow, itemView: View, anchor: View) {
        val screenHeight: Int =
            Utils.getScreenHeight(anchor.context) - Utils.dip2Px(itemView.context, 44f)
        //wrap_content会导致第一次得到的view的高宽是0
        itemView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        // 计算contentView的高宽
        val popupWidth = itemView.measuredWidth
        var popupHeight = itemView.measuredHeight
        Timber.d("popupHeight:$popupHeight,screenHeight:$screenHeight")
        popupHeight = if (popupHeight > screenHeight) {
            screenHeight
        } else {
            popupHeight
        }
        popupWindow.height = popupHeight
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)

        popupWindow.showAtLocation(
            anchor, Gravity.NO_GRAVITY,
            (location[0] + anchor.width / 2) - popupWidth / 2,
            location[1] - popupHeight
        )
    }

    open fun initPopupWindow(itemView: View): PopupWindow {
        val popupWindow = PopupWindow(
            itemView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        popupWindow.width = ViewGroup.LayoutParams.WRAP_CONTENT //设置宽度
        popupWindow.height = ViewGroup.LayoutParams.WRAP_CONTENT //设置高度
        //popupWindow.setBackgroundDrawable(ColorDrawable(Color.parseColor("#11171E"))) //设置背景颜色
        popupWindow.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                itemView.context,
                R.drawable.bg_rounded
            )
        )
        popupWindow.isOutsideTouchable = false //设置可否触摸外部取消显示
        popupWindow.isFocusable = true //设置焦点
        popupWindow.elevation = 10F;

        return popupWindow
    }

    open fun initPopupWindowIfNeed(itemView: View, type: Int) {
        if (C.TRACK_TYPE_AUDIO == type) {
            if (audioWindow == null) {
                audioWindow = initPopupWindow(itemView)
            }
        }
        if (C.TRACK_TYPE_TEXT == type) {
            if (textWindow == null) {
                textWindow = initPopupWindow(itemView)
            }
        }
        if (C.TRACK_TYPE_METADATA == type) {
            if (metaWindow == null) {
                metaWindow = initPopupWindow(itemView)
            }
        }
    }

    open fun hideALlPopupWindow() {
        textWindow?.dismiss()
    }

    open fun showSpeedWindow(
        context: Context,
        btnSpeed: View,
        speedListener: SpeedAdapter.SelectSpeedListener?
    ) {
        if (null == speedHolder) {
            speedHolder = SpeedHolder(context, context.getString(R.string.player_track_video_speed))
            val view = LayoutInflater.from(context).inflate(R.layout.track_fragment, null)
            speedHolder!!.setView(view)
        }
        speedHolder!!.setListener() { pos, speed ->
            speedListener?.selected(pos, speed)
            speedWindow?.dismiss()
        }
        if (null == speedWindow) {
            speedWindow = initPopupWindow(speedHolder!!.itemView!!)
        }
        showPopupWindow(speedWindow!!, speedHolder!!.itemView!!, btnSpeed)
    }

    open fun setCapsData(
    ) {
    }

    open fun clearOld() {
        speedHolder = null
        audioWindow = null
        textWindow = null
        metaWindow = null
        speedWindow = null
        resolutionWindow = null
    }
}