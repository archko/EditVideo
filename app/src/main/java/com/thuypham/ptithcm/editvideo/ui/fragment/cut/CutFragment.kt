package com.thuypham.ptithcm.editvideo.ui.fragment.cut

import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.util.Util
import com.google.android.material.slider.RangeSlider
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentCutBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.toTimeAsHHmmSSS
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.ui.activity.ResultActivity
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.viewmodel.CutViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*

class CutFragment : BaseFragment<FragmentCutBinding>(R.layout.fragment_cut) {

    private val cutViewModel: CutViewModel by viewModel()

    private lateinit var formatBuilder: StringBuilder
    private lateinit var formatter: Formatter

    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var hasInitPlayer = false
    private var startTime = 0f
    private var endTime = 0f

    private var resultUrl: String? = null

    private var isAspectMenuShown = false
    private var mTvAspectCustom: TextView? = null
    private var mTvAspectSquare: TextView? = null
    private var mTvAspectPortrait: TextView? = null
    private var mTvAspectLandscape: TextView? = null
    private var mTvAspect4by3: TextView? = null
    private var mTvAspect16by9: TextView? = null
    private var mAspectMenu: View? = null

    override fun setupView() {
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        setupToolbar()
        setupRangeSlider()

        mAspectMenu = binding.root.findViewById(R.id.aspectMenu)
        mTvAspectCustom = binding.root.findViewById(R.id.tvAspectCustom)
        mTvAspectSquare = binding.root.findViewById(R.id.tvAspectSquare)
        mTvAspectPortrait = binding.root.findViewById(R.id.tvAspectPortrait)
        mTvAspectLandscape = binding.root.findViewById(R.id.tvAspectLandscape)
        mTvAspect4by3 = binding.root.findViewById(R.id.tvAspect4by3)
        mTvAspect16by9 = binding.root.findViewById(R.id.tvAspect16by9)

        setupEvent()
        binding.ivPlay.setOnSingleClickListener { playPause() }
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.title_cut_video))
        setLeftBtn(R.drawable.ic_back) {
            goBack()
        }
    }

    private fun goBack() {
        requireActivity().finish()
    }

    override fun setupLogic() {
        super.setupLogic()
        resultUrl = arguments?.getString(HomeFragment.RESULT_PATH)
        fetchVideoInfo(resultUrl)
        binding.cropVideoView.setOnBoxChangedListener { x1, y1, x2, y2 ->
            binding.tvCropRect.text = "box:[$x1,$y1],[${x2},${y2}]"
        }
    }

    private fun setupEvent() {
        binding.ivDone.setOnSingleClickListener {
            startCrop()
        }
        binding.ivAspectRatio.setOnClickListener { v -> handleMenuVisibility() }
        mTvAspectCustom!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(false)
            handleMenuVisibility()
        }
        mTvAspectSquare!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(true)
            binding.cropVideoView.setAspectRatio(10, 10)
            handleMenuVisibility()
        }
        mTvAspectPortrait!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(true)
            binding.cropVideoView.setAspectRatio(8, 16)
            handleMenuVisibility()
        }
        mTvAspectLandscape!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(true)
            binding.cropVideoView.setAspectRatio(16, 8)
            handleMenuVisibility()
        }
        mTvAspect4by3!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(true)
            binding.cropVideoView.setAspectRatio(4, 3)
            handleMenuVisibility()
        }
        mTvAspect16by9!!.setOnClickListener { v ->
            binding.cropVideoView.setFixedAspectRatio(true)
            binding.cropVideoView.setAspectRatio(16, 9)
            handleMenuVisibility()
        }
    }

    private fun handleMenuVisibility() {
        isAspectMenuShown = !isAspectMenuShown
        val interpolator: TimeInterpolator = if (isAspectMenuShown) {
            DecelerateInterpolator()
        } else {
            AccelerateInterpolator()
        }
        val translationY: Float =
            if (isAspectMenuShown) 0f else Resources.getSystem().displayMetrics.density * 400
        val alpha: Float = if (isAspectMenuShown) 1f else 0f
        mAspectMenu!!.animate()
            .translationY(translationY)
            .alpha(alpha)
            .setInterpolator(interpolator)
            .start()
    }

    private fun playPause() {
        if (player?.isPlaying!!) {
            player?.pause()
            binding.ivPlay.setImageResource(R.drawable.ic_play)
            return
        }
        player?.play()
        binding.ivPlay.setImageResource(R.drawable.ic_pause)
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

    private fun setSlider() {
        binding.apply {
            try {
                if ((player?.getDuration() ?: 0) > 0) {
                    rangeSlider.isVisible = true
                    startTime = 0f
                    endTime = player?.getDuration()?.toFloat() ?: 0f
                    rangeSlider.valueTo = endTime
                    rangeSlider.values = arrayListOf(0f, endTime)
                    tvDurationStart.text = rangeSlider.values[0].toLong().toTimeAsHHmmSSS()
                    tvDurationEnd.text = rangeSlider.values[1].toLong().toTimeAsHHmmSSS()

                    tvDuration.text = Util.getStringForTime(
                        formatBuilder,
                        formatter,
                        endTime.toLong()
                    )
                } else {
                    rangeSlider.isVisible = false
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun fetchVideoInfo(uri: String?) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(File(uri).absolutePath)
        val videoWidth =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val videoHeight =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        val rotationDegrees =
            Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION))
        binding.cropVideoView.initBounds(videoWidth, videoHeight, rotationDegrees)
    }

    override fun setupDataObserver() {
        super.setupDataObserver()
        cutViewModel.cutResponse.observe(viewLifecycleOwner) { response ->
            hideLoading()
            when (response) {
                is ResponseHandler.Success -> {
                    hideLoading()
                    ResultActivity.start(requireActivity(), response.data, 0)
                }
                is ResponseHandler.Loading -> {
                    showLoading()
                }
                is ResponseHandler.Failure -> {
                    hideLoading()
                    showSnackBar("Failed to crop!")
                }
                else -> {
                    hideLoading()
                }
            }
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

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
            binding.ivPlay.setImageResource(R.drawable.ic_pause)
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
                binding.cropVideoView.setPlayer(exoPlayer)
                resultUrl?.let { exoPlayer.setMediaItem(MediaItem.fromUri(it)) }
                exoPlayer.playWhenReady = playWhenReady
                exoPlayer.seekTo(currentWindow, playbackPosition)
                exoPlayer.addListener(playbackStateListener)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                exoPlayer.prepare()
                binding.ivPlay.setImageResource(R.drawable.ic_play)
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

        /**
         * 每次重新定位就会触发该方法
         */
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            if (!hasInitPlayer) {
                hasInitPlayer = true
                binding.ivPlay.setImageResource(R.drawable.ic_pause)
                setSlider()
            }
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
        binding.clBottomMenu.visibility = View.VISIBLE
        binding.cropVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH)
    }

    private fun landscape() {
        binding.toolbar.toolbarContainer.visibility = View.GONE
        binding.clBottomMenu.visibility = View.GONE
        binding.cropVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT)
    }

    private fun startCrop() {
        if (TextUtils.isEmpty(resultUrl)) {
            return
        }
        val cropRect = binding.cropVideoView.cropRect
        /*val startCrop = mTmbProgress!!.leftProgress
        val durationCrop = mTmbProgress!!.rightProgress - mTmbProgress!!.leftProgress
        var start = Util.getStringForTime(formatBuilder, formatter, startCrop)
        var duration = Util.getStringForTime(formatBuilder, formatter, durationCrop)
        start += "." + startCrop % 1000
        duration += "." + durationCrop % 1000
        Log.d("TAG", "Screen Cropping : $startCrop----$durationCrop")*/

        if (player?.isPlaying!!) {
            player?.pause()
            binding.ivPlay.setImageResource(R.drawable.ic_play)
        }
        cutViewModel.cutVideo(
            cropRect.right - cropRect.left,
            cropRect.bottom - cropRect.top,
            cropRect.left,
            cropRect.top,
            resultUrl!!
        )
    }
}

