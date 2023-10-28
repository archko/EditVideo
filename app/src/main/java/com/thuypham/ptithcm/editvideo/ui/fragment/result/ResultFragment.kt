package com.thuypham.ptithcm.editvideo.ui.fragment.result

import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.Util
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentResultBinding
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.extension.shareImageToOtherApp
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.dialog.ConfirmDialog
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.viewmodel.ResultViewModel
import io.flutter.plugins.exoplayer.ExoSourceFactory
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class ResultFragment : BaseFragment<FragmentResultBinding>(R.layout.fragment_result) {

    private val resultViewModel: ResultViewModel by viewModel()

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var resultUrl: String? = null

    override fun setupLogic() {
        super.setupLogic()
        resultUrl = arguments?.getString(HomeFragment.RESULT_PATH)
        binding.tvOutputPath.text = resultUrl
    }

    override fun setupView() {
        setupToolbar()
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        resultViewModel.deleteFileResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    requireActivity().finish()
                }
                is ResponseHandler.Loading -> {
                }
                is ResponseHandler.Failure -> {
                }
                else -> {
                }
            }
        }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.result))
        setRightBtn(R.drawable.ic_delete) {
            showDialogDeleteConfirm()
        }
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
        setSubRightBtn(R.drawable.ic_share) {
            resultUrl?.let { it1 -> shareImageToOtherApp(it1) }
        }
    }

    private fun showDialogDeleteConfirm() {
        ConfirmDialog(
            title = getString(R.string.dialog_msg_delete_title),
            msg = getString(R.string.dialog_msg_delete_project),
            cancelMsg = getString(R.string.dialog_cancel),
            isShowCancelMsg = true,
            onConfirmClick = {
                resultUrl?.let { lifecycleScope.launch { resultViewModel.deleteFile(it) } }
            },
            onCancelClick = {
            }).show(
            parentFragmentManager,
            ConfirmDialog.TAG
        )
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

    private fun initializePlayer() {
        player = ExoSourceFactory.buildPlayer(requireContext())
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                resultUrl?.let { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
            }
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        when (newConfig.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> portrait()
            Configuration.ORIENTATION_LANDSCAPE -> landscape()
            else -> {
                portrait()
            }
        }
    }

    private fun portrait() {
        binding.toolbar.toolbarContainer.visibility = View.VISIBLE
        binding.tvOutputPath.visibility = View.VISIBLE
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    }

    private fun landscape() {
        binding.toolbar.toolbarContainer.visibility = View.GONE
        binding.tvOutputPath.visibility = View.GONE
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    }
}
