package com.archko.editvideo.ui.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_NONE
import androidx.media3.ui.DefaultTrackNameProvider
import androidx.media3.ui.PlayerView
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.extension.IntentFile
import com.thuypham.ptithcm.editvideo.extension.getScreenWidth
import io.flutter.plugins.exoplayer.ExoSourceFactory
import timber.log.Timber
import java.util.*

/**
 * @author: archko 2023/6/26 :14:14
 */
@UnstableApi
open class MExoPlayerActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateVisibilityAction: Runnable
    private var updateProgressAction: Runnable? = null
    private lateinit var updateSeekAction: Runnable
    private lateinit var trackNameProvider: DefaultTrackNameProvider

    private lateinit var styledPlayerView: PlayerView
    private lateinit var touchPlayerView: View
    private lateinit var tips: TextView
    private var mExoPlayer: ExoPlayer? = null
    private var mediaItem: MediaItem? = null
    private var videoPlayerDelegate: VideoPlayerDelegate? = null

    private var url: String? = null
    private lateinit var sensorHelper: SensorHelper

    private lateinit var btnAudio: View
    private lateinit var btnMore: View
    private lateinit var btnSpeed: TextView
    private lateinit var bottomBar: View
    private lateinit var titleBar: View
    private lateinit var back: View
    private lateinit var layoutSeekbar: View
    private lateinit var layoutBasicControls: View
    private lateinit var seekBar: SeekBar
    private lateinit var videoName: TextView
    private lateinit var durationView: TextView
    private lateinit var positionView: TextView
    private lateinit var btnPlay: ImageView
    private lateinit var btnOrientation: View
    private lateinit var layoutError: View
    private lateinit var errorMessageView: TextView
    private lateinit var btnReplay: Button
    private lateinit var btnExit: Button
    private lateinit var progressWaiting: View
    private lateinit var mLockView: ImageView

    private var currentIndex: Int = 0

    private var startAutoPlay = true
    private var startItemIndex = 0

    /**
     * 视频的进度位置
     */
    private var startPosition: Long = 0

    /**
     * 准备切换字幕
     */
    private var readyToSelectTrack: Boolean = false

    /**
     * 正在seek,不更新进度条
     */
    private var isSeeking: Boolean = false

    /**
     * 按下左右键,进度条响应时间,超出后,快进快退
     */
    private var seekTime: Long = 0

    //显示过缓冲,拖动的时候就不显示了,一次播放只显示一次缓冲,或者可以在一定时间段外再显示
    private var showingBuffer = true

    private val checkBufferRunnable: Runnable = Runnable {
        mExoPlayer?.run {
            Timber.d("play.bufferedPercentage:${mExoPlayer!!.bufferedPercentage},playing:${mExoPlayer!!.isPlaying}")
            if (mExoPlayer!!.bufferedPercentage < 30 && !mExoPlayer!!.isPlaying) {
                showingBuffer = true
                showBuffer()
            } else {
                showingBuffer = false
            }
        }
    }

    private var formatBuilder = StringBuilder()
    private var formatter = java.util.Formatter(formatBuilder, Locale.getDefault())

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBarHelper.hideSystemUI(this)

        sensorHelper = SensorHelper(this)

        trackNameProvider = DefaultTrackNameProvider(resources)

        setContentView(R.layout.m_player)
        styledPlayerView = findViewById(R.id.styled_player_view)
        touchPlayerView = findViewById(R.id.touch_player_view)
        tips = findViewById(R.id.tips)

        //去除字幕背景
        val captionStyleCompat = CaptionStyleCompat(
            Color.WHITE,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            EDGE_TYPE_NONE,
            Color.WHITE,
            null
        )
        styledPlayerView.subtitleView?.setStyle(captionStyleCompat)

        btnAudio = findViewById(R.id.btn_audio)
        /*btnAudio.setOnClickListener {
            tracksHelper.showAudioTracks(
                mExoPlayer,
                this,
                btnAudio,
                trackNameProvider
            )
        }*/
        btnMore = findViewById(R.id.btn_more)
        /*btnMore.setOnClickListener {
            tracksHelper.showMetaTracks(
                mExoPlayer,
                this,
                url,
                btnMore,
                trackNameProvider
            )
        }*/
        btnSpeed = findViewById(R.id.btn_speed)
        //btnSpeed.setOnClickListener { showSpeedWindow() }

        bottomBar = findViewById(R.id.bottom_bar)
        titleBar = findViewById(R.id.layout_titlebar)
        back = findViewById(R.id.back)
        layoutSeekbar = findViewById(R.id.layout_seekbar)
        layoutBasicControls = findViewById(R.id.layout_basic_controls)
        seekBar = findViewById(R.id.progress)
        videoName = findViewById(R.id.txt_video_name)
        durationView = findViewById(R.id.duration)
        positionView = findViewById(R.id.position)
        btnPlay = findViewById(R.id.btn_play)
        btnOrientation = findViewById(R.id.btn_orientation)
        layoutError = findViewById(R.id.layout_error)
        errorMessageView = findViewById(R.id.error_message)
        btnReplay = findViewById(R.id.btn_replay)
        btnExit = findViewById(R.id.btn_exit)
        progressWaiting = findViewById(R.id.progress_waiting)
        mLockView = findViewById(R.id.tv_lock);

        back.setOnClickListener {
            postFinish()
        }
        btnPlay.setOnClickListener {
            mExoPlayer?.run {
                dispatchPlayPause(this)
            }
        }
        btnOrientation.setOnClickListener {
            setOritation()
        }
        mLockView.setOnClickListener {
            videoPlayerDelegate?.toggleLock()
            showLock()
            val lock = videoPlayerDelegate?.isLock
            if (null != lock && lock) {
                updateControls()
            }
        }

        /*seekBar.setListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartPreview(seekBar: SeekBar?, progress: Int) {
                if (!isSeeking) {   //第一次按下,记录时间
                    seekTime = SystemClock.uptimeMillis()
                }
                isSeeking = true
                if (SystemClock.uptimeMillis() - seekTime > 700L) {
                    handler.postDelayed(updateSeekAction, 50L)
                }
            }

            override fun onStopPreview(seekBar: SeekBar?, progress: Int) {
                isSeeking = false
                restoreSeekIncrement()
                if (seekBar != null) {
                    mExoPlayer?.seekTo(seekBar.progress.toLong())
                }
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (isSeeking) {
                    positionView.text =
                        Util.getStringForTime(formatBuilder, formatter, progress.toLong())
                }
            }
        })*/

        btnReplay.setOnClickListener {
            layoutError.visibility = View.GONE
            mExoPlayer?.let { player -> dispatchPlayPause(player) }
        }
        btnExit.setOnClickListener {
            finish()
        }

        updateProgressAction = Runnable { updateProgress() }
        updateVisibilityAction = Runnable { updateControls() }
        updateSeekAction = Runnable { updateSeekIncrement() }

        if (null == videoPlayerDelegate) {
            videoPlayerDelegate = VideoPlayerDelegate(this@MExoPlayerActivity)
        }
        videoPlayerDelegate!!.setDelegateTouchListener(object :
            VideoPlayerDelegate.DelegateTouchListener {
            override fun click() {
                if (videoPlayerDelegate!!.isLock) {
                    handler.postDelayed(updateVisibilityAction, CONTROLS_SHOW_MS)
                    showLock()
                    return
                }

                if (bottomBar.visibility == View.GONE) {
                    showControls()
                } else {
                    updateControls()
                }
            }

            override fun speed() {
                if (tips.visibility == View.GONE) {
                    tips.visibility = View.VISIBLE
                }
                tips.text = String.format(resources.getString(R.string.player_tip_3speed))
            }

            override fun volumeChange(last: Int, current: Int) {
                if (tips.visibility == View.GONE) {
                    tips.visibility = View.VISIBLE
                }
                tips.text = String.format(resources.getString(R.string.player_tip_volume), current)
            }

            override fun brightnessChange(current: Double) {
                if (tips.visibility == View.GONE) {
                    tips.visibility = View.VISIBLE
                }
                tips.text = String.format(
                    resources.getString(R.string.player_tip_brightness),
                    (100 * current).toInt()
                )
            }

            override fun seek(change: Long) {
                mExoPlayer?.run {
                    if (tips.visibility == View.GONE) {
                        tips.visibility = View.VISIBLE
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
                    tips.text = text
                }
            }

            override fun seekEnd(changed: Long) {
                mExoPlayer?.run {
                    val pos = currentPosition
                    seekTo(pos + changed)
                }
                if (tips.visibility == View.VISIBLE) {
                    tips.visibility = View.GONE
                }
            }

            override fun hideTip() {
                isSeeking = false
                if (tips.visibility == View.VISIBLE) {
                    tips.visibility = View.GONE
                }
            }

        })
        touchPlayerView.setOnTouchListener(videoPlayerDelegate)

        if (null == intent) {
            finish()
            showToast("播放错误")
            return
        }

        url = intent.data?.let { IntentFile.getFilePathByUri(this, it) }

        Timber.d("play.url:$url")

        if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                postFinish()
            }
        } else {
            onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        postFinish()
                    }
                })
        }

        initializePlayer()
    }

    private fun postFinish() {
        val ori = sensorHelper.getOrientation()
        if (ori == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
            btnSpeed.visibility = View.GONE
            btnMore.visibility = View.GONE
            sensorHelper.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        } else {
            finish()
        }
    }

    private fun setOritation() {
        val ori = sensorHelper.getOrientation()
        if (ori == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            sensorHelper.setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
            btnSpeed.visibility = View.VISIBLE
            btnMore.visibility = View.VISIBLE
        } else {
            btnSpeed.visibility = View.GONE
            btnMore.visibility = View.GONE
            sensorHelper.setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    private fun showSpeedWindow() {
        /*tracksHelper.showSpeedWindow(this, btnSpeed) { _, speed ->
            if (speed.speed == 1.0f) {
                btnSpeed.text = getString(R.string.player_speed)
            } else {
                btnSpeed.text = speed.text
            }
            mExoPlayer?.setPlaybackSpeed(speed.speed)
        }*/
    }

    //=================== init player ===================
    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        setVideoName()
        if (mExoPlayer == null) {
            mediaItem = ExoSourceFactory.createMediaItem(
                url,
                this@MExoPlayerActivity
            )
            mExoPlayer = ExoSourceFactory.buildPlayer(applicationContext)
            mExoPlayer!!.addListener(playerListener)
            mExoPlayer!!.addAnalyticsListener(EventLogger())
            mExoPlayer!!.setAudioAttributes(AudioAttributes.DEFAULT, true)
            mExoPlayer!!.playWhenReady = startAutoPlay
            styledPlayerView.player = mExoPlayer

            if (null == videoPlayerDelegate) {
                videoPlayerDelegate = VideoPlayerDelegate(this@MExoPlayerActivity)
            }
            videoPlayerDelegate!!.setExoPlayer(mExoPlayer)

            prepare()
        } else {
            prepare()
        }
    }

    private fun prepare() {
        //val haveStartPosition = startItemIndex != C.INDEX_UNSET
        Timber.d("play.prepare.position:$startPosition")
        if (startPosition > 0) {
            mExoPlayer!!.seekTo(startPosition)
        }
        mediaItem?.let { mExoPlayer!!.setMediaItem(it, startPosition <= 0) }
        showingBuffer = true
        mExoPlayer!!.prepare()
        mExoPlayer!!.play()
        showControls()
    }

    private fun setVideoName() {
        videoName.text = url//MediaUtil.inferName(url.playerData.getVideoName())
    }

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            super.onEvents(player, events)
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButton()
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            super.onTimelineChanged(timeline, reason)
            updateTimeline()
        }

        //暂停再开始也会调用这个
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            Timber.d("play.onRenderedFirstFrame")
            updateTimeline()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            Timber.d("play.onIsPlayingChanged:$isPlaying")
        }

        override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
            if (playbackState == Player.STATE_ENDED) {
                autoToNextVideo()
                layoutBasicControls.visibility = View.VISIBLE
                bottomBar.visibility = View.VISIBLE
                return
            }

            if (playbackState == Player.STATE_BUFFERING) {
                if (showingBuffer) {
                    showBuffer()
                } else {
                    handler.postDelayed(checkBufferRunnable, 1000L)
                }
            } else {
                //if (playbackState == Player.STATE_READY || playbackState == Player.STATE_IDLE) {
                hideBuffer()
            }

            if (playbackState == Player.STATE_READY) {
                showingBuffer = false
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.d("play.error:${error.errorCode}")
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                mExoPlayer!!.seekToDefaultPosition()
                mExoPlayer!!.prepare()
            } else {
                showControls()
                /*if (error.errorCode in 2000..2999) { //io异常,可能是网不好,超时等
                    showToast(R.string.player_error_net_error)
                } else if (error.errorCode in 3000..3999) { //内容解析异常,不处理
                    showToast(R.string.player_error_codec_error)
                } else if (error.errorCode in 4000..4999) { //解码异常
                    showToast(R.string.player_error_codec_error)
                } else if (error.errorCode in 5000..5999) { //音频异常

                }*/
            }
            processPlayError(error)
        }

        override fun onTracksChanged(tracks: Tracks) {
        }
    }

    private fun autoToNextVideo() {
    }

    private fun showBuffer() {
        handler.removeCallbacks(checkBufferRunnable)
        if (isSeeking) {
            return
        }
        if (progressWaiting.visibility == View.GONE) {
            progressWaiting.visibility = View.VISIBLE
        }
    }

    private fun hideBuffer() {
        if (progressWaiting.visibility == View.VISIBLE) {
            progressWaiting.visibility = View.GONE
        }
        showingBuffer = false
    }

    private fun updateControls() {
        bottomBar.visibility = View.GONE
        titleBar.visibility = View.GONE
        mLockView.visibility = View.GONE
    }

    private fun showLock() {
        mLockView.setVisibility(View.VISIBLE)
        updateLock()
    }

    private fun updateLock() {
        val lock = videoPlayerDelegate?.isLock
        if (null != lock && lock) {
            mLockView.setImageResource(R.drawable.ic_lock)
        } else {
            mLockView.setImageResource(R.drawable.ic_unlock)
        }
    }

    private fun showControls() {
        layoutBasicControls.visibility = View.VISIBLE
        bottomBar.visibility = View.VISIBLE
        titleBar.visibility = View.VISIBLE
        mLockView.setVisibility(View.VISIBLE)
        updateLock()

        delayHideControls()
    }

    private fun delayHideControls() {
        handler.removeCallbacks(updateVisibilityAction)
        handler.postDelayed(updateVisibilityAction, CONTROLS_SHOW_MS)
    }

    private fun showError(errorTxt: String) {
        errorMessageView.text = errorTxt
        layoutError.visibility = View.VISIBLE
    }

    private fun processPlayError(error: PlaybackException) {
        Timber.d("play.error:${error.errorCode}")
    }

    private fun showToast(msgId: Int) {
        Toast.makeText(this, msgId, Toast.LENGTH_LONG).show()
    }

    private fun showToast(msg: String) {
        if (TextUtils.isEmpty(msg)) {
            return
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    //=================== play controller start ===================
    private fun dispatchPlayPause(player: Player) {
        val state: @Player.State Int = player.playbackState
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !player.playWhenReady) {
            dispatchPlay(player)
        } else {
            dispatchPause(player)
        }
    }

    private fun dispatchPlay(player: Player) {
        val state: @Player.State Int = player.playbackState
        if (state == Player.STATE_IDLE && player.isCommandAvailable(Player.COMMAND_PREPARE)) {
            player.prepare()
        } else if (state == Player.STATE_ENDED
            && player.isCommandAvailable(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)
        ) {
            player.seekToDefaultPosition()
        }
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.play()

            //修正在ok键按下,暂停后再播放时,底部无法隐藏的问题
            delayHideControls()
        }
    }

    private fun dispatchPause(player: Player) {
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.pause()
        }
    }

    @OptIn(UnstableApi::class)
    private fun updatePlayPauseButton() {
        val shouldShowPauseButton: Boolean = shouldShowPauseButton()
        @DrawableRes val drawableRes =
            if (shouldShowPauseButton) R.drawable.ic_pause else R.drawable.ic_play
        btnPlay.setImageDrawable(Util.getDrawable(this, resources, drawableRes))
    }

    private fun shouldShowPauseButton(): Boolean {
        return mExoPlayer != null
                && mExoPlayer!!.playbackState != Player.STATE_ENDED
                && mExoPlayer!!.playbackState != Player.STATE_IDLE
                && mExoPlayer!!.playWhenReady
    }

    @OptIn(UnstableApi::class)
    private fun updateTimeline() {
        val duration = mExoPlayer?.duration?.toInt() ?: 0
        seekBar.max = duration
        seekBar.keyProgressIncrement = duration / 1000
        durationView.text = Util.getStringForTime(formatBuilder, formatter, duration.toLong())
        updateProgress()
        Timber.d("play.updateTimeline:${seekBar.max}")
    }

    @OptIn(UnstableApi::class)
    private fun updateProgress() {
        if (positionView.visibility == View.GONE) {
            return
        }
        if (isSeeking) {
            return
        }
        val player: Player? = this.mExoPlayer
        var position: Long = 0
        if (player != null) {
            position = player.currentPosition
        }
        positionView.text = Util.getStringForTime(formatBuilder, formatter, position)

        if (seekBar.max <= 1) {
            seekBar.progress = 0
        } else {
            seekBar.progress = position.toInt()
        }

        positionView.removeCallbacks(updateProgressAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            positionView.postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            positionView.postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS)
        }
    }

    private fun updateStartPosition() {
        if (mExoPlayer != null) {
            startAutoPlay = mExoPlayer!!.playWhenReady
            startItemIndex = mExoPlayer!!.currentMediaItemIndex
            //startPosition = Math.max(0, mExoPlayer!!.currentPosition)
        }
    }

    private fun updateSeekIncrement() {
        val duration = mExoPlayer?.duration?.toInt() ?: 0
        Timber.d("play.onKeyDown.updateSeekIncrement:$duration")
        if (duration > 0) {
            seekBar.keyProgressIncrement = duration / 100
        }
    }

    private fun restoreSeekIncrement() {
        handler.removeCallbacks(updateSeekAction)
        val duration = mExoPlayer?.duration?.toInt() ?: 0
        if (duration > 0) {
            seekBar.keyProgressIncrement = duration / 1000
        }
    }

    //=================== play controller end ===================
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Timber.d("play.onNewIntent:${intent?.data}")
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()

        videoPlayerDelegate?.resetBrightness()
    }

    private fun releasePlayer() {
        if (mExoPlayer != null) {
            updateStartPosition()
            mExoPlayer!!.release()
            mExoPlayer = null
            styledPlayerView.player = null
            mediaItem = null
        }
    }

    override fun onStart() {
        super.onStart()
        if (VERSION.SDK_INT > 23) {
            resumePlay()
        }
    }

    override fun onResume() {
        super.onResume()
        if (VERSION.SDK_INT <= 23) {
            resumePlay()
        }
    }

    private fun resumePlay() {
        mExoPlayer?.play()
        if (styledPlayerView != null) {
            styledPlayerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (VERSION.SDK_INT <= 23) {
            mExoPlayer?.pause()
            if (styledPlayerView != null) {
                styledPlayerView.onPause()
            }
        }
    }

    override fun onStop() {
        super.onStop()

        if (VERSION.SDK_INT > 23) {
            mExoPlayer?.pause()
            if (styledPlayerView != null) {
                styledPlayerView.onPause()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateStartPosition()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        videoPlayerDelegate?.updateScreenWidth(getScreenWidth())
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        StatusBarHelper.hideSystemUI(this)
    }

    companion object {

        const val TAG = "ExoPlayer"
        private const val MAX_UPDATE_INTERVAL_MS = 1000L

        //控制器显示时长,5秒隐藏
        private const val CONTROLS_SHOW_MS = 5000L
    }
}
