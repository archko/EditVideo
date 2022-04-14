package com.thuypham.ptithcm.editvideo.ui.activity

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.ItemTouchHelper
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMergeBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.util.ItemTouchCallback

class MergeActivity : BaseActivity<ActivityMergeBinding>(R.layout.activity_merge) {

    private val data: List<String> = listOf(
        "A",
        "B",
        "C",
        "D",
        "E",
        "F",
        "G",
        "H",
        "I",
        "J",
        "K",
        "L",
        "M",
        "N",
        "O",
        "P",
        "Q",
        "R",
        "S",
        "T",
        "U",
        "V",
        "W",
        "X",
        "Y",
        "Z"
    )
    private val menuAdapter: MergeAdapter by lazy {
        MergeAdapter(this)
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.root.findViewById<AppCompatTextView>(R.id.tvTitle).apply {
            show()
            text = title
            setOnSingleClickListener { finish() }
        }
    }

    private fun setupRecyclerView() {
        binding.apply {
            recyclerView.adapter = menuAdapter
            menuAdapter.data = data
        }

        val helper = ItemTouchHelper(
            ItemTouchCallback(
                menuAdapter
            )
        )
        helper.attachToRecyclerView(binding.recyclerView)
    }
}