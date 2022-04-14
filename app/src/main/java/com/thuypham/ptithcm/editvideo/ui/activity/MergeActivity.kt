package com.thuypham.ptithcm.editvideo.ui.activity

import android.content.Intent
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.ItemTouchHelper
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseActivity
import com.thuypham.ptithcm.editvideo.databinding.ActivityMergeBinding
import com.thuypham.ptithcm.editvideo.extension.getPath
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.util.ItemTouchCallback
import com.thuypham.ptithcm.editvideo.viewmodel.MergeViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MergeActivity : BaseActivity<ActivityMergeBinding>(R.layout.activity_merge) {

    private val mergeViewModel: MergeViewModel by viewModel()
    private val mediaFileList: MutableList<MediaFile> = ArrayList()
    private val mergeAdapter: MergeAdapter by lazy {
        MergeAdapter(this) { mediaFile, pos -> onMediaClick(mediaFile, pos) }
    }

    private fun onMediaClick(mediaFile: MediaFile, pos: Int) {
        mediaFileList.remove(mediaFile)
        mergeAdapter.data = mediaFileList
        mergeAdapter.notifyDataSetChanged()
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
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
            recyclerView.adapter = mergeAdapter
            mergeAdapter.data = mediaFileList
            mergeAdapter.notifyDataSetChanged()
        }

        val helper = ItemTouchHelper(
            ItemTouchCallback(
                mergeAdapter
            )
        )
        helper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupEvent() {
        binding.btnAddVideo.setOnSingleClickListener {
            addVideo()
        }
    }

    private fun addVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "audio/*"))
            .addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            intent,
            HomeFragment.REQUEST_SAF_FFMPEG
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HomeFragment.REQUEST_SAF_FFMPEG && resultCode == RESULT_OK && data != null && data.data != null) {
            binding.apply {
                val mediaFile = MediaFile()
                mediaFile.getPath(this@MergeActivity, data.data!!)
                mediaFileList.add(mediaFile)
            }
        }
    }
}