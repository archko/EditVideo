package com.archko.editvideo.tracks

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thuypham.ptithcm.editvideo.R

/**
 * @author: archko 2023/6/29 :15:46
 */
class TracksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var exoCheck: ImageView
    var exoText: TextView

    init {
        exoCheck = itemView.findViewById(R.id.exo_check)
        exoText = itemView.findViewById(R.id.exo_text)
    }
}