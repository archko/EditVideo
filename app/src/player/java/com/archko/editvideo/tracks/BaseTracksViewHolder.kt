package com.archko.editvideo.tracks

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.R

/**
 * @author: archko 2023/6/29 :14:35
 */
abstract class BaseTracksViewHolder(
    var ctx: Context,
    var trackType: Int,
    var title: String,
    var selectTrackListener: TracksAdapter.SelectTrackListener?
) {
    var tracksAdapter: TracksAdapter? = null

    lateinit var titleView: TextView
    lateinit var trackView: RecyclerView
    var itemView: View? = null

    open fun setView(view: View) {
        itemView = view
        titleView = view.findViewById(R.id.title)
        trackView = view.findViewById(R.id.track_view)
        trackView.itemAnimator = null

        titleView.text = title
        trackView.layoutManager = LinearLayoutManager(ctx)

        initTrackSelectionAdapter()
    }

    fun getString(resId: Int): String {
        return ctx.getString(resId)
    }

    open fun initTrackSelectionAdapter() {
        tracksAdapter = TracksAdapter(trackType, object : TracksAdapter.SelectTrackListener {
            override fun selected(
                position: Int,
                trackInformations: List<TrackInformation>,
                trackType: Int
            ) {
                val trackInformation: TrackInformation = trackInformations.get(position)
                if (trackInformation.type == TrackInformation.TYPE_SELECT_TRACK ||
                    TrackInformation.TYPE_DOWNLOAD_TRACK == trackInformation.type
                ) {
                    selectTrackListener?.selected(position, trackInformations, trackType)
                } else {
                    setTrack(trackType, position, trackInformations)
                }
            }
        })
        trackView.adapter = tracksAdapter

        tracksAdapter!!.trackInformations = getAllTracks()
        tracksAdapter!!.notifyDataSetChanged()
    }

    abstract fun setTrack(
        trackType: Int,
        index: Int,
        trackInformations: List<TrackInformation>
    )

    abstract fun getAllTracks(): List<TrackInformation>

    fun notifyDataSetChanged() {
        tracksAdapter?.notifyDataSetChanged()
    }

    fun resetTextTracks() {
        tracksAdapter?.reset()
    }
}