package com.thuypham.ptithcm.editvideo.ui.fragment.merge

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
import com.thuypham.ptithcm.editvideo.ui.activity.ResultActivity
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.util.ItemTouchCallback
import com.thuypham.ptithcm.editvideo.viewmodel.MergeViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MergeFragment : BaseFragment<ActivityMergeBinding>(R.layout.activity_merge) {

    private val mergeViewModel: MergeViewModel by sharedViewModel()
    private val mediaFileList: ArrayList<MediaFile> = ArrayList()
    private val mergeAdapter: MergeAdapter by lazy {
        MergeAdapter(requireContext(),
            object : MergeAdapter.ItemListener {
                override fun onDelete(item: MediaFile, pos: Int) {
                    this@MergeFragment.onDelete(item, pos)
                }

                override fun onPlay(item: MediaFile, pos: Int) {
                    this@MergeFragment.onClick(item, pos)
                }
            }
        )
    }

    private fun onDelete(mediaFile: MediaFile, pos: Int) {
        mediaFileList.remove(mediaFile)
        mergeAdapter.data = mediaFileList
        mergeAdapter.notifyDataSetChanged()
    }

    private fun onClick(mediaFile: MediaFile, pos: Int) {
        mediaFile.path?.let {
            PlayerFragment(it).show(
                parentFragmentManager,
                ConfirmDialog.TAG
            )
        }
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.result))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
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

                    ResultActivity.start(requireContext(), response.data, 0)
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    hideLoading()
                    showSnackBar("merge video error!")
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
        if (mediaFileList.size <= 1) {
            showSnackBar("Please add more video!")
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