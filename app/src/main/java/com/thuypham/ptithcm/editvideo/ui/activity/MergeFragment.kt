package com.thuypham.ptithcm.editvideo.ui.activity

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.ActivityMergeBinding
import com.thuypham.ptithcm.editvideo.extension.getPath
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.util.ItemTouchCallback
import com.thuypham.ptithcm.editvideo.viewmodel.MergeViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MergeFragment : BaseFragment<ActivityMergeBinding>(R.layout.activity_merge) {

    private val mergeViewModel: MergeViewModel by sharedViewModel()
    private val mediaFileList: ArrayList<MediaFile> = ArrayList()
    private val mergeAdapter: MergeAdapter by lazy {
        MergeAdapter(requireContext()) { mediaFile, pos -> onMediaClick(mediaFile, pos) }
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
        setToolbarTitle(getString(R.string.result))
        setRightBtn(R.drawable.ic_delete) {

        }
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
        setSubRightBtn(R.drawable.ic_share) {

        }
        setSubRight2Btn(R.drawable.ic_edit) {

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
        binding.btnMergeVideo.setOnSingleClickListener {
            mergeVideo()
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()

        mergeViewModel.editVideoResponse.observe(this) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    Log.d("merge", response.data)
                    hideLoading()

                    ResultActivity.start(requireContext(), response.data)
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    hideLoading()
                }
                else -> {
                    hideLoading()
                }
            }
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

    private fun mergeVideo() {
        if (mediaFileList.size < 1) {
            showSnackBar("Please add video!")
            return
        }
        mergeViewModel.mergeVideo(mediaFileList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HomeFragment.REQUEST_SAF_FFMPEG && resultCode == RESULT_OK && data != null && data.data != null) {
            binding.apply {
                val mediaFile = MediaFile()
                mediaFile.getPath(requireContext(), data.data!!)
                val count = mergeAdapter.itemCount
                mediaFileList.add(mediaFile)
                mergeAdapter.notifyItemInserted(count)
            }
        }
    }
}