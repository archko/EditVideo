package com.thuypham.ptithcm.editvideo.ui.activity

import androidx.appcompat.widget.AppCompatTextView
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMergeBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.Menu
import com.thuypham.ptithcm.editvideo.ui.fragment.home.MenuAdapter

class MergeActivity : BaseActivity<ActivityMergeBinding>(R.layout.activity_merge) {

    private val menuAdapter: MenuAdapter by lazy {
        MenuAdapter { menu -> onMenuClick(menu) }
    }

    private fun onMenuClick(menu: Menu) {
        if (menu.id == Menu.MENU_MERGE_VIDEO) {

            return
        }
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
            menuAdapter.submitList(MenuAdapter.listMenu)
        }
    }
}