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
class SpeedHolder(var ctx: Context, var title: String) {

    private lateinit var titleView: TextView
    private var speedAdapter: SpeedAdapter? = null

    private lateinit var recyclerView: RecyclerView
    var itemView: View? = null

    fun setView(view: View) {
        itemView = view
        titleView = view.findViewById(R.id.title)
        recyclerView = view.findViewById(R.id.track_view)
        recyclerView.itemAnimator = null

        titleView.text = title
        recyclerView.layoutManager = LinearLayoutManager(ctx)
        //recyclerView.addItemDecoration(SpaceItemDecoration(Utils.dip2Px(ctx, 4.0f)))

        initAdapter()
    }

    private fun initAdapter() {
        speedAdapter = SpeedAdapter(null)
        recyclerView.adapter = speedAdapter

        speedAdapter!!.notifyDataSetChanged()
    }

    fun setListener(speedListener: SpeedAdapter.SelectSpeedListener) {
        speedAdapter?.setSelectSpeedListener(speedListener)
    }
}