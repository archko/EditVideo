package com.archko.editvideo.tracks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.DefaultTrackNameProvider
import com.thuypham.ptithcm.editvideo.R

/**
 * @author: archko 2023/7/3 :17:36
 */
@OptIn(UnstableApi::class)
class TracksHelper : AbsTracksHelper() {

    private var audioTrackHolder: ExoTracksViewHolder? = null
    private var textTrackHolder: ExoTracksViewHolder? = null

    fun showAudioTracks(
        mExoPlayer: ExoPlayer?,
        context: Context,
        btn_audio: View,
        trackNameProvider: DefaultTrackNameProvider
    ) {
        if (mExoPlayer == null) {
            return
        }
        if (null == audioTrackHolder) {
            audioTrackHolder = ExoTracksViewHolder(mExoPlayer,
                context, C.TRACK_TYPE_AUDIO,
                context.getString(R.string.player_track_video_audio),
                trackNameProvider,
                object : TracksAdapter.SelectTrackListener {
                    override fun selected(
                        position: Int,
                        trackInformations: List<TrackInformation>,
                        trackType: Int
                    ) {
                        audioWindow?.dismiss()
                    }
                })
            val view = LayoutInflater.from(context).inflate(R.layout.track_fragment, null)
            audioTrackHolder!!.setView(view)
        }
        audioTrackHolder!!.initTracksIfNeeded(mExoPlayer, C.TRACK_TYPE_AUDIO)

        initPopupWindowIfNeed(audioTrackHolder!!.itemView!!, C.TRACK_TYPE_AUDIO)
        showPopupWindow(audioWindow!!, audioTrackHolder!!.itemView!!, btn_audio)
    }

    override fun initPopupWindowIfNeed(itemView: View, type: Int) {
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

    fun showTextTracks(
        mExoPlayer: ExoPlayer?,
        context: Context,
        btn_text: View,
        trackNameProvider: DefaultTrackNameProvider,
        selectTrackListener: TracksAdapter.SelectTrackListener?
    ) {
        if (mExoPlayer == null) {
            return
        }
        if (null == textTrackHolder) {
            textTrackHolder = ExoTracksViewHolder(
                mExoPlayer,
                context,
                C.TRACK_TYPE_TEXT,
                context.getString(R.string.player_track_video_subtitle),
                trackNameProvider,
                object : TracksAdapter.SelectTrackListener {
                    override fun selected(
                        position: Int,
                        trackInformations: List<TrackInformation>,
                        trackType: Int
                    ) {
                        selectTrackListener?.selected(position, trackInformations, trackType)
                        textWindow?.dismiss()
                    }
                }
            )
            val view = LayoutInflater.from(context).inflate(R.layout.track_fragment, null)
            textTrackHolder!!.setView(view)
        }
        textTrackHolder!!.initTracksIfNeeded(mExoPlayer, C.TRACK_TYPE_TEXT)

        initPopupWindowIfNeed(textTrackHolder!!.itemView!!, C.TRACK_TYPE_TEXT)
        showPopupWindow(textWindow!!, textTrackHolder!!.itemView!!, btn_text)
    }

    fun resetTextTracks(exoPlayer: ExoPlayer?) {
        audioTrackHolder?.mExoPlayer = exoPlayer
        textTrackHolder?.mExoPlayer = exoPlayer
        textTrackHolder?.resetTextTracks()
    }

    override fun clearOld() {
        super.clearOld()
        audioTrackHolder = null
        textTrackHolder = null
    }
}