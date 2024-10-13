package com.archko.editvideo.tracks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.R

/**
 * @author: archko 2023/6/29 :15:45
 */
class TracksAdapter(
    private var trackType: Int,
    private var selectTrackListener: SelectTrackListener?
) : RecyclerView.Adapter<TracksViewHolder>() {
    var trackInformations: List<TrackInformation> = arrayListOf()

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TracksViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return TracksViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: TracksViewHolder, position: Int) {
        val information = trackInformations[position]
        viewHolder.exoText.text = information.trackName
        viewHolder.itemView.setOnClickListener { view: View? ->
            selectTrackListener?.selected(position, trackInformations, trackType)
            view?.isSelected = true
            notifyDataSetChanged()
        }
        if (information.isSelected) {
            viewHolder.exoCheck.setImageResource(R.drawable.ic_track_check)
        } else {
            viewHolder.exoCheck.setImageBitmap(null)
        }
    }

    override fun getItemCount(): Int {
        return trackInformations.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun reset() {
        trackInformations = arrayListOf()
        notifyDataSetChanged()
    }

    interface SelectTrackListener {

        fun selected(position: Int, trackInformations: List<TrackInformation>, trackType: Int)
    }
}