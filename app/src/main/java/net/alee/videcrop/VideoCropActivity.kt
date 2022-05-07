package net.alee.videcrop

import android.animation.TimeInterpolator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.exoplayer2.util.Util
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.model.ResponseHandler
import com.thuypham.ptithcm.editvideo.viewmodel.CutViewModel
import net.alee.videcrop.VideoCropActivity
import net.alee.videcrop.cropview.window.CropVideoView
import net.alee.videcrop.player.VideoPlayer
import net.alee.videcrop.player.VideoPlayer.OnProgressUpdateListener
import net.alee.videcrop.view.ProgressView
import net.alee.videcrop.view.VideoSliceSeekBarH
import net.alee.videcrop.view.VideoSliceSeekBarH.SeekBarChangeListener
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File
import java.util.*

class VideoCropActivity : AppCompatActivity(), OnProgressUpdateListener, SeekBarChangeListener {

    private var mVideoPlayer: VideoPlayer? = null
    private lateinit var formatBuilder: StringBuilder
    private lateinit var formatter: Formatter
    private var mIvPlay: AppCompatImageView? = null
    private var mIvAspectRatio: AppCompatImageView? = null
    private var mIvDone: AppCompatImageView? = null
    private var mTmbProgress: VideoSliceSeekBarH? = null
    private var mCropVideoView: CropVideoView? = null
    private var mTvProgress: TextView? = null
    private var mTvDuration: TextView? = null
    private var mTvAspectCustom: TextView? = null
    private var mTvAspectSquare: TextView? = null
    private var mTvAspectPortrait: TextView? = null
    private var mTvAspectLandscape: TextView? = null
    private var mTvAspect4by3: TextView? = null
    private var mTvAspect16by9: TextView? = null
    private var mTvCropProgress: TextView? = null
    private var mAspectMenu: View? = null
    private var mProgressBar: ProgressView? = null
    private var inputPath: String? = null
    private var isVideoPlaying = false
    private var isAspectMenuShown = false
    private val cutViewModel: CutViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        inputPath = intent.getStringExtra(VIDEO_CROP_INPUT_PATH)
        Log.d("paths", "inputPath--$inputPath")
        if (TextUtils.isEmpty(inputPath)) {
            Toast.makeText(
                this,
                "input and output paths must be valid and not null",
                Toast.LENGTH_SHORT
            ).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        findViews()
        initListeners()
        initPlayer(inputPath)
        //requestStoragePermission()
        ////----------/////////////-------///////////

        //CropCustom()
        //CropDone()
    }

    override fun onStart() {
        super.onStart()
        if (isVideoPlaying) {
            mVideoPlayer?.play(true)
        }
    }

    public override fun onStop() {
        super.onStop()
        mVideoPlayer?.play(false)
    }

    public override fun onDestroy() {
        mVideoPlayer?.release()
        super.onDestroy()
    }

    override fun onFirstTimeUpdate(duration: Long, currentPosition: Long) {
        mTmbProgress!!.setSeekBarChangeListener(this)
        mTmbProgress!!.setMaxValue(duration)
        mTmbProgress!!.leftProgress = 0
        mTmbProgress!!.rightProgress = duration
        mTmbProgress!!.setProgressMinDiff(0)
    }

    override fun onProgressUpdate(currentPosition: Long, duration: Long, bufferedPosition: Long) {
        mTmbProgress!!.videoPlayingProgress(currentPosition)
        if (!mVideoPlayer?.isPlaying!! || currentPosition >= mTmbProgress!!.rightProgress) {
            if (mVideoPlayer?.isPlaying == true) {
                playPause()
            }
        }
        mTmbProgress!!.setSliceBlocked(false)
        mTmbProgress!!.removeVideoStatusThumb()

//        mTmbProgress.setPosition(currentPosition)
//        mTmbProgress.setBufferedPosition(bufferedPosition)
//        mTmbProgress.setDuration(duration)
    }

    private fun findViews() {
        mCropVideoView = findViewById(R.id.cropVideoView)
        mIvPlay = findViewById(R.id.ivPlay)
        mIvAspectRatio = findViewById(R.id.ivAspectRatio)
        mIvDone = findViewById(R.id.ivDone)
        mTvProgress = findViewById(R.id.tvProgress)
        mTvDuration = findViewById(R.id.tvDuration)
        mTmbProgress = findViewById(R.id.tmbProgress)
        mAspectMenu = findViewById(R.id.aspectMenu)
        mTvAspectCustom = findViewById(R.id.tvAspectCustom)
        mTvAspectSquare = findViewById(R.id.tvAspectSquare)
        mTvAspectPortrait = findViewById(R.id.tvAspectPortrait)
        mTvAspectLandscape = findViewById(R.id.tvAspectLandscape)
        mTvAspect4by3 = findViewById(R.id.tvAspect4by3)
        mTvAspect16by9 = findViewById(R.id.tvAspect16by9)
        mProgressBar = findViewById(R.id.pbCropProgress)
        mTvCropProgress = findViewById(R.id.tvCropProgress)
    }

    private fun initListeners() {
        mIvPlay!!.setOnClickListener { v: View? -> playPause() }
        mIvAspectRatio!!.setOnClickListener { v: View? -> handleMenuVisibility() }
        mTvAspectCustom!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(false)
            handleMenuVisibility()
        }
        mTvAspectSquare!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(true)
            mCropVideoView!!.setAspectRatio(10, 10)
            handleMenuVisibility()
        }
        mTvAspectPortrait!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(true)
            mCropVideoView!!.setAspectRatio(8, 16)
            handleMenuVisibility()
        }
        mTvAspectLandscape!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(true)
            mCropVideoView!!.setAspectRatio(16, 8)
            handleMenuVisibility()
        }
        mTvAspect4by3!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(true)
            mCropVideoView!!.setAspectRatio(4, 3)
            handleMenuVisibility()
        }
        mTvAspect16by9!!.setOnClickListener { v: View? ->
            mCropVideoView!!.setFixedAspectRatio(true)
            mCropVideoView!!.setAspectRatio(16, 9)
            handleMenuVisibility()
        }
        mIvDone!!.setOnClickListener { v: View? -> handleCropStart() }

        cutViewModel.cutResponse.observe(this) { response ->
            when (response) {
                is ResponseHandler.Success -> {
                    mIvDone?.setEnabled(true)
                    mIvPlay?.setEnabled(true)
                    mProgressBar?.setVisibility(View.INVISIBLE)
                    mProgressBar?.setProgress(0)
                    mTvCropProgress?.setVisibility(View.INVISIBLE)
                    mTvCropProgress?.setText("0%")
                    Toast.makeText(this@VideoCropActivity, "FINISHED", Toast.LENGTH_SHORT).show()
                }
                is ResponseHandler.Loading -> {
                }
                is ResponseHandler.Failure -> {
                    Toast.makeText(this@VideoCropActivity, "Failed to crop!", Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                }
            }
        }
    }

    private fun playPause() {
        isVideoPlaying = !mVideoPlayer?.isPlaying!!
        if (mVideoPlayer?.isPlaying!!) {
            mVideoPlayer?.play(!mVideoPlayer?.isPlaying!!)
            mTmbProgress!!.setSliceBlocked(false)
            mTmbProgress!!.removeVideoStatusThumb()
            mIvPlay!!.setImageResource(R.drawable.ic_play)
            return
        }
        mVideoPlayer?.seekTo(mTmbProgress!!.leftProgress)
        mVideoPlayer?.play(!mVideoPlayer?.isPlaying!!)
        mTmbProgress!!.videoPlayingProgress(mTmbProgress!!.leftProgress)
        mIvPlay!!.setImageResource(R.drawable.ic_pause)
    }

    private fun initPlayer(uri: String?) {
        if (!File(uri).exists()) {
            Toast.makeText(this, "File doesn't exists", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        mVideoPlayer = VideoPlayer(this)
        mCropVideoView!!.setPlayer(mVideoPlayer?.player)
        mVideoPlayer?.initMediaSource(this, uri)
        mVideoPlayer?.setUpdateListener(this)
        fetchVideoInfo(uri)
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
        mCropVideoView!!.initBounds(videoWidth, videoHeight, rotationDegrees)
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

    @SuppressLint("DefaultLocale")
    private fun handleCropStart() {
        val cropRect = mCropVideoView!!.cropRect
        val startCrop = mTmbProgress!!.leftProgress
        val durationCrop = mTmbProgress!!.rightProgress - mTmbProgress!!.leftProgress
        var start = Util.getStringForTime(formatBuilder, formatter, startCrop)
        var duration = Util.getStringForTime(formatBuilder, formatter, durationCrop)
        start += "." + startCrop % 1000
        duration += "." + durationCrop % 1000
        Log.d(TAG, "Screen Cropping : $startCrop----$durationCrop")

        /* String[] cmd = {
        "-i", inputPath, "-filter:v", "crop=" + 240 + ":" + 120 + ":" + 100 + ":" + 100, "-c:a", "copy", outputPath
        }*/
        cutViewModel.cutVideo(
            cropRect.right - cropRect.left,
            cropRect.bottom - cropRect.top,
            cropRect.left,
            cropRect.top,
            inputPath!!
        )
        /*
        val crop = String.format(
            "crop=%d:%d:%d:%d:exact=0",
            cropRect.right,
            cropRect.bottom,
            cropRect.left,
            cropRect.top
        )
        String[] cmd = {
                "-y",
                "-ss",
                start,
                "-i",
                inputPath,
                "-t",
                duration,
                "-vf",
                crop,
                outputPath
        }
        mFFTask = mFFMpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
            @Override
            public void onSuccess(String message) {
                setResult(RESULT_OK)
                Log.e(TAG, message)
                finish()
            }

            @Override
            public void onProgress(String message) {
                Log.e(TAG, message)
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(VideoCropActivity.this, "Failed to crop!", Toast.LENGTH_SHORT).show()
                Log.e(TAG, message)
            }

            @Override
            public void onProgressPercent(float percent) {
                mProgressBar.setProgress((int) percent)
                mTvCropProgress.setText((int) percent + "%")
                Log.d(TAG, "Progress percent : " + String.valueOf(percent))
            }

            @Override
            public void onStart() {
                mIvDone.setEnabled(false)
                mIvPlay.setEnabled(false)
                mProgressBar.setVisibility(View.VISIBLE)
                mProgressBar.setProgress(0)
                mTvCropProgress.setVisibility(View.VISIBLE)
                mTvCropProgress.setText("0%")
            }

            @Override
            public void onFinish() {
                mIvDone.setEnabled(true)
                mIvPlay.setEnabled(true)
                mProgressBar.setVisibility(View.INVISIBLE)
                mProgressBar.setProgress(0)
                mTvCropProgress.setVisibility(View.INVISIBLE)
                mTvCropProgress.setText("0%")
                Toast.makeText(VideoCropActivity.this, "FINISHED", Toast.LENGTH_SHORT).show()
                //finish()
            }
        }, durationCrop * 1.0f / 1000)*/
    }

    override fun seekBarValueChanged(leftThumb: Long, rightThumb: Long) {
        if (mTmbProgress!!.selectedThumb == 1) {
            mVideoPlayer?.seekTo(leftThumb)
        }
        mTvDuration!!.text = Util.getStringForTime(
            formatBuilder,
            formatter,
            rightThumb
        )
        mTvProgress!!.text = Util.getStringForTime(
            formatBuilder,
            formatter,
            leftThumb
        )
    }

    fun CropCustom() {
        mCropVideoView!!.setFixedAspectRatio(true)
        mCropVideoView!!.setAspectRatio(9, 9)
        // handleMenuVisibility()
    }

    fun CropDone() {
        handleCropStart()
    }

    companion object {
        private const val VIDEO_CROP_INPUT_PATH = "VIDEO_CROP_INPUT_PATH"
        private const val VIDEO_CROP_OUTPUT_PATH = "VIDEO_CROP_OUTPUT_PATH"
        private const val STORAGE_REQUEST = 100
        private const val TAG = "MYTAGCROP"
        fun startIntent(context: Context, inputPath1: String?, outputPath1: String?) {
            val intent = Intent(context, VideoCropActivity::class.java)
            intent.putExtra(VIDEO_CROP_INPUT_PATH, inputPath1)
            intent.putExtra(VIDEO_CROP_OUTPUT_PATH, outputPath1)
            context.startActivity(intent)
        }
    }
}