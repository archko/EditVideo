package com.thuypham.ptithcm.editvideo.ui.fragment.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Handler
import android.util.Log
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.RangeSlider
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentHomeBinding
import com.thuypham.ptithcm.editvideo.extension.getPath
import com.thuypham.ptithcm.editvideo.extension.gone
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.toTimeAsHHmmSSS
import com.thuypham.ptithcm.editvideo.model.MediaFile
import com.thuypham.ptithcm.editvideo.model.Menu
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.activity.MergeActivity
import com.thuypham.ptithcm.editvideo.ui.activity.ResultActivity
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.viewmodel.MediaViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    companion object {
        const val RESULT_PATH = "RESULT_PATH"
        const val REQUEST_SAF_FFMPEG = 12
    }

    private val mediaViewModel: MediaViewModel by sharedViewModel()

    private val menuAdapter: MenuAdapter by lazy {
        MenuAdapter { menu -> onMenuClick(menu) }
    }

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var currentMediaFile: MediaFile? = null

    private var mediaItem: MediaItem? = null
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var startTime = 0f
    private var endTime = 0f
    private var currentMenuId = Menu.MENU_CUT_VID

    private var shouldNavigateToResultFragment = false

    private fun onMenuClick(menu: Menu) {
        if (menu.id == Menu.MENU_MERGE_VIDEO) {
            val intent = Intent(requireActivity(), MergeActivity::class.java)
            startActivity(intent)
            return
        }
        if (currentMediaFile == null) {
            showSnackBar(R.string.empty_file_msg)
            return
        }
        currentMenuId = menu.id
        shouldNavigateToResultFragment = true
        when (menu.id) {
            Menu.MENU_CUT_VID -> {
                if (startTime == 0f && endTime == currentMediaFile?.duration?.toFloat()) {
                    showDialogCutVideoConfirm()
                } else {
                    player?.stop()
                    currentMediaFile?.path?.let { mediaViewModel.cutVideo(startTime, endTime, it) }
                }
            }
            Menu.MENU_EXTRACT_IMAGES -> {
                currentMediaFile?.path?.let { mediaViewModel.extractImages(startTime, endTime, it) }
            }
            Menu.MENU_EXTRACT_AUDIO -> {

            }
            Menu.MENU_REVERSE_VIDEO -> {

            }
            Menu.MENU_CONVERT_TO_GIF -> {

            }
            Menu.MENU_SPLIT_VIDEO -> {

            }
            Menu.MENU_MERGE_VIDEO -> {
                val intent = Intent(requireActivity(), MergeActivity::class.java)
                startActivity(intent)
            }
            else -> {

            }
        }
    }

    private fun showDialogCutVideoConfirm() {
        ConfirmDialog(
            title = getString(R.string.dialog_cut_video_title),
            msg = getString(R.string.dialog_cut_video_description),
            okMsg = getString(R.string.dialog_ok),
        ).show(parentFragmentManager, ConfirmDialog.TAG)
    }

    override fun setupView() {
        setupToolbar()
        setupRecyclerView()
        setupEvent()
        setupRangeSlider()
    }

    private fun setupRangeSlider() {
        binding.apply {
            rangeSlider.setLabelFormatter { value ->
                val time = value.toLong().toTimeAsHHmmSSS()
                time
            }

            rangeSlider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: RangeSlider) {
                }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: RangeSlider) {
                    startTime = rangeSlider.values[0]
                    endTime = rangeSlider.values[1]
                    tvDurationStart.text = startTime.toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = endTime.toLong().toTimeAsHHmmSSS()
                    player?.seekTo(startTime.toLong())
                }

            })
        }
    }

    private fun setupEvent() {
        binding.btnUploadVideo.setOnSingleClickListener {
            /*navigateTo(
                R.id.media,
                bundleOf(
                    MediaFragment.MEDIA_TYPE to MediaFile.MEDIA_TYPE_VIDEO,
                    MediaFragment.MAX_SELECTED_COUNT to 1
                )
            )*/

            val intent = Intent(Intent.ACTION_GET_CONTENT)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*", "audio/*"))
                .addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(
                intent,
                REQUEST_SAF_FFMPEG
            )
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.app_name))
    }

    private fun setupRecyclerView() {
        binding.apply {
            rvMenu.adapter = menuAdapter
            menuAdapter.submitList(MenuAdapter.listMenu)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SAF_FFMPEG && resultCode == RESULT_OK && data != null && data.data != null) {
            binding.apply {
                layoutEmptyVideo.gone()
                currentMediaFile = MediaFile()
                currentMediaFile!!.getPath(requireActivity(), data.data!!)
                setMediaItem()
            }
        }
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        mediaViewModel.currentMedia.observe(viewLifecycleOwner) {
            binding.apply {
                layoutEmptyVideo.gone()
                currentMediaFile = it
                setMediaItem()
            }
        }
        mediaViewModel.editVideoResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    navigateToResultFragment(response.data)
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    hideLoading()
                    response.extra?.let { showSnackBar(it) }
                }
                else -> {
                    hideLoading()
                }
            }
        }
    }

    private fun navigateToResultFragment(resultPath: String) {
        if (shouldNavigateToResultFragment) {
            val destinationId = when (currentMenuId) {
                Menu.MENU_CUT_VID -> R.id.homeToResult
                Menu.MENU_EXTRACT_IMAGES -> R.id.home_to_extractImages
                Menu.MENU_EXTRACT_AUDIO -> R.id.homeToResult
                Menu.MENU_REVERSE_VIDEO -> R.id.homeToResult
                Menu.MENU_CONVERT_TO_GIF -> R.id.homeToResult
                Menu.MENU_SPLIT_VIDEO -> R.id.homeToResult
                else -> R.id.homeToResult
            }
            shouldNavigateToResultFragment = false
            //navigateTo(destinationId, bundleOf(RESULT_PATH to resultPath))

            val intent = Intent(requireActivity(), ResultActivity::class.java)
            intent.putExtra(RESULT_PATH, resultPath)
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun setMediaItem() {
        Log.d(this::class.java.name, "changed $currentMediaFile")
        mediaItem = currentMediaFile?.path?.let { MediaItem.fromUri(it) }
        player?.setMediaItem(mediaItem!!)
        player?.prepare()
        setSlider()

        runnable = Runnable {
            if (player?.currentPosition ?: 0 >= endTime) {
                player?.seekTo(startTime.toLong())
            }
            runnable.let { handler.postDelayed(it, 1000) }
        }
        handler = Handler()
        handler.postDelayed(runnable, 1000)
    }

    private fun setSlider() {
        binding.apply {
            videoView.hideController()
            try {
                if (currentMediaFile?.duration ?: 0 > 0) {
                    rangeSlider.isVisible = true
                    startTime = 0f
                    endTime = currentMediaFile?.duration?.toFloat() ?: 0f
                    rangeSlider.valueTo = endTime
                    rangeSlider.values = arrayListOf(0f, endTime)
                    tvDurationStart.text = rangeSlider.values[0].toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = rangeSlider.values[1].toLong().toTimeAsHHmmSSS()
                } else {
                    rangeSlider.isVisible = false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer

                mediaItem?.let { exoPlayer.setMediaItem(it) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        player = null
    }

    private val playbackStateListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                else -> "UNKNOWN_STATE             -"
            }
            Log.d(this::class.java.name, "changed state to $stateString")
        }

        /**
         * 每次重新定位就会触发该方法
         */
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            Log.d(
                this::class.java.name,
                "changed onRenderedFirstFrame ${currentMediaFile?.duration}"
            )
            //系统文件管理器,有可能获取不到,它是xx/primary/下面的,只有路径,默认设置了10毫秒,所以需要更新.
            currentMediaFile?.let {
                if (null != it.duration && it.duration!! <= 10) {
                    it.duration = player?.duration
                    setSlider()
                }
            }
        }
    }
}

