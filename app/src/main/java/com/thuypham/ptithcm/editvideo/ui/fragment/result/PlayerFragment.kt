package com.thuypham.ptithcm.editvideo.ui.fragment.result

import android.content.res.Configuration
import android.util.Log
import android.view.View
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.Util
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentPlayerBinding
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import com.thuypham.ptithcm.editvideo.extension.goBack
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.util.VideoPlayerDelegate
import com.thuypham.ptithcm.editvideo.viewmodel.ResultViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class PlayerFragment : BaseFragment<FragmentPlayerBinding>(R.layout.fragment_player) {

    private val resultViewModel: ResultViewModel by viewModel()

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private var resultUrl: String? = null
    private var videoPlayerDelegate: VideoPlayerDelegate? = null

    /**
     * 正在seek,不更新进度条
     */
    private var isSeeking: Boolean = false
    private var formatBuilder = StringBuilder()
    private var formatter = java.util.Formatter(formatBuilder, java.util.Locale.getDefault())

    override fun setupLogic() {
        super.setupLogic()
        resultUrl = arguments?.getString(HomeFragment.RESULT_PATH)
    }

    override fun setupView() {
        setupToolbar()
        if (null == videoPlayerDelegate) {
            videoPlayerDelegate = activity?.let { VideoPlayerDelegate(it) }
        }
        videoPlayerDelegate!!.setDelegateTouchListener(object :
            VideoPlayerDelegate.DelegateTouchListener {
            override fun click() {
                if (binding.toolbar.toolbarContainer.visibility == View.GONE) {
                    binding.toolbar.toolbarContainer.visibility = View.VISIBLE
                    binding.videoView.showController()
                } else {
                    binding.videoView.hideController()
                    binding.toolbar.toolbarContainer.visibility = View.GONE
                }
            }

            override fun speed() {
                if (binding.tips.visibility == View.GONE) {
                    binding.tips.visibility = View.VISIBLE
                }
                binding.tips.text = String.format(resources.getString(R.string.player_tip_3speed))
            }

            override fun volumeChange(last: Int, current: Int) {
                if (binding.tips.visibility == View.GONE) {
                    binding.tips.visibility = View.VISIBLE
                }
                binding.tips.text =
                    String.format(resources.getString(R.string.player_tip_volume), current)
            }

            override fun brightnessChange(current: Double) {
                if (binding.tips.visibility == View.GONE) {
                    binding.tips.visibility = View.VISIBLE
                }
                binding.tips.text = String.format(
                    resources.getString(R.string.player_tip_brightness),
                    (100 * current).toInt()
                )
            }

            override fun seek(change: Long) {
                player?.run {
                    if (binding.tips.visibility == View.GONE) {
                        binding.tips.visibility = View.VISIBLE
                    }
                    var pos = currentPosition + change
                    if (pos > duration) {
                        pos = duration
                    }
                    if (pos < 0) {
                        pos = 0
                    }
                    val text = String.format(
                        "%s/%s",
                        Util.getStringForTime(
                            formatBuilder,
                            formatter,
                            pos
                        ),
                        Util.getStringForTime(
                            formatBuilder,
                            formatter,
                            duration
                        )
                    )
                    binding.tips.text = text
                }
            }

            override fun seekEnd(changed: Long) {
                player?.run {
                    val pos = currentPosition
                    seekTo(pos + changed)
                }
                if (binding.tips.visibility == View.VISIBLE) {
                    binding.tips.visibility = View.GONE
                }
            }

            override fun hideTip() {
                isSeeking = false
                if (binding.tips.visibility == View.VISIBLE) {
                    binding.tips.visibility = View.GONE
                }
            }

        })
        binding.touchPlayerView.setOnTouchListener(videoPlayerDelegate)

        initializePlayer()
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
        resultUrl?.let { setToolbarTitle(it) }
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            player?.play()
            binding.videoView.onResume()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            player?.play()
            binding.videoView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            player?.pause()
            binding.videoView.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            player?.pause()
            binding.videoView.onPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        videoPlayerDelegate?.resetBrightness()
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
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer
                resultUrl?.let { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
                videoPlayerDelegate?.setExoPlayer(exoPlayer)
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
        videoPlayerDelegate?.updateScreenWidth(getScreenWidth())
    }

    private fun portrait() {
        binding.toolbar.toolbarContainer.visibility = View.VISIBLE
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
    }

    private fun landscape() {
        binding.toolbar.toolbarContainer.visibility = View.GONE
        binding.videoView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    }
}
