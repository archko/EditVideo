package com.thuypham.ptithcm.editvideo.ui.fragment.cmd

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.SessionState
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentCmdBinding
import com.thuypham.ptithcm.editvideo.extension.IntentFile
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show
import com.thuypham.ptithcm.editvideo.ui.fragment.home.HomeFragment
import com.thuypham.ptithcm.editvideo.viewmodel.CmdViewModel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class CmdDialogFragment(
) : BaseDialogFragment<FragmentCmdBinding>(R.layout.fragment_cmd) {

    override val isFullScreen = true
    private lateinit var handler: Handler
    private val cmdViewModel: CmdViewModel by viewModel()

    companion object {
        const val CMD_CUT =
            "-ss 00:00 -i /sdcard/DCIM/aa.mp4 -t 60 -c copy -preset ultrafast /sdcard/DCIM/o.mp4"
        const val CMD_MERGE =
            "-ss 00:00 -i /sdcard/DCIM/aa.mp4 -i /sdcard/DCIM/aa.aac -vcodec copy -acodec copy /sdcard/DCIM/o.mp4"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)
        dialog.window?.setGravity(Gravity.CENTER)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        handler = Handler(Looper.getMainLooper())
        return dialog
    }

    override fun setupView() {
        setupToolbar()
        binding.apply {
            cmdFfmpeg.setOnSingleClickListener { runFFmpeg() }
            cmdFfprobe.setOnSingleClickListener { runFFprobe() }
            cmdCut.setOnSingleClickListener { setCutCmd() }
            cmdMerge.setOnSingleClickListener { setMergeCmd() }
            cmdInput.setOnSingleClickListener { selectMedia() }
        }
        setupDataObserver()
    }

    private fun setupToolbar() {
        setToolbarTitle(getString(R.string.menu_run_cmd))
        setLeftBtn(R.drawable.ic_back) {
            this@CmdDialogFragment.dismiss()
        }
    }

    private fun setToolbarTitle(title: String, onClick: ((View) -> Unit?)? = null) {
        binding.root.findViewById<AppCompatTextView>(R.id.tvTitle).apply {
            show()
            text = title
            setOnSingleClickListener { onClick?.invoke(this) }
        }
    }

    private fun setLeftBtn(iconResID: Int, onClick: ((View) -> Unit?)? = null) {
        binding.root.findViewById<AppCompatImageView>(R.id.ivLeft).apply {
            show()
            setImageResource(iconResID)
            setOnSingleClickListener { onClick?.invoke(this) }
        }
    }

    override fun setupDataObserver() {
        cmdViewModel.logResponse.observe(viewLifecycleOwner) { log ->
            handler.post { appendOutput(log.message) }
        }

        cmdViewModel.ffmpegResponse.observe(viewLifecycleOwner) { session ->
            val state = session.state
            val returnCode = session.returnCode
            Log.d(
                "TAG",
                String.format(
                    "FFmpeg process exited with state %s and rc %s.%s",
                    FFmpegKitConfig.sessionStateToString(state),
                    returnCode,
                    notNull(session.failStackTrace, "\n")
                )
            )
            if (state == SessionState.FAILED || !returnCode.isValueSuccess) {
                showSnackBar("Command failed. Please check output for the details.")
            }
        }
        cmdViewModel.ffprobeResponse.observe(viewLifecycleOwner) { session ->
            val state = session.state
            val returnCode = session.returnCode
            appendOutput(session.output)
            Log.d(
                "TAG",
                String.format(
                    "FFprobe process exited with state %s and rc %s.%s",
                    FFmpegKitConfig.sessionStateToString(state),
                    returnCode,
                    notNull(
                        session.failStackTrace,
                        "\n"
                    )
                )
            )
            if (state == SessionState.FAILED || !session.returnCode.isValueSuccess) {
                showSnackBar("Command failed. Please check output for the details.")
            }
        }
    }

    private fun runFFmpeg() {
        clearOutput()
        val ffmpegCommand = String.format("%s", binding.editCmd.text.toString())
        Log.d("TAG", String.format("Current log level is %s.", FFmpegKitConfig.getLogLevel()))
        Log.d("TAG", String.format("FFmpeg process started with arguments:\n'%s'", ffmpegCommand))
        lifecycleScope.launch { cmdViewModel.runFFmpeg(ffmpegCommand) }
        listFFmpegSessions()
    }

    private fun runFFprobe() {
        clearOutput()
        val ffprobeCommand = String.format("%s", binding.editCmd.text.toString())
        Log.d(
            "TAG",
            String.format("FFprobe process started with arguments:\n'%s'", ffprobeCommand)
        )

        lifecycleScope.launch { cmdViewModel.runFFprobe(ffprobeCommand) }
        listFFprobeSessions()
    }

    private fun setCutCmd() {
        binding.editCmd.setText(CMD_CUT)
    }

    private fun setMergeCmd() {
        binding.editCmd.setText(CMD_MERGE)
    }

    private fun selectMedia() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("*/*"))
            .addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(
            intent,
            HomeFragment.REQUEST_SAF_FFMPEG
        )
    }

    private fun appendOutput(logMessage: String?) {
        binding.tvOutput.append(logMessage)
        binding.scrollView.scrollBy(0, 1500)
    }

    private fun clearOutput() {
        binding.tvOutput.text = ""
        binding.scrollView.scrollBy(0, 1500)
    }

    private fun notNull(string: String?, valuePrefix: String?): String {
        return if (string == null) "" else String.format("%s%s", valuePrefix, string)
    }

    private fun listFFmpegSessions() {
        val ffmpegSessions = FFmpegKit.listSessions()
        Log.d("TAG", "Listing FFmpeg sessions.")
        for (i in ffmpegSessions.indices) {
            val session = ffmpegSessions[i]
            Log.d(
                "TAG",
                String.format(
                    "Session %d = id:%d, startTime:%s, duration:%s, state:%s, returnCode:%s.",
                    i,
                    session.sessionId,
                    session.startTime,
                    session.duration,
                    session.state,
                    session.returnCode
                )
            )
        }
        Log.d("TAG", "Listed FFmpeg sessions.")
    }

    private fun listFFprobeSessions() {
        val ffprobeSessions = FFprobeKit.listFFprobeSessions()
        Log.d("TAG", "Listing FFprobe sessions.")
        for (i in ffprobeSessions.indices) {
            val session = ffprobeSessions[i]
            Log.d(
                "TAG", String.format(
                    "Session %d = id:%d, startTime:%s, duration:%s, state:%s, returnCode:%s.",
                    i,
                    session.sessionId,
                    session.startTime,
                    session.duration,
                    session.state,
                    session.returnCode
                )
            )
        }
        Log.d("TAG", "Listed FFprobe sessions.")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HomeFragment.REQUEST_SAF_FFMPEG && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            binding.apply {
                val path = IntentFile.getPath(requireActivity(), data.data!!)
                var oriString = editCmd.text.toString()
                var selection = editCmd.selectionStart
                if (selection < oriString.length) {
                    val sb = StringBuilder(oriString)
                    sb.insert(selection, path)
                    editCmd.setText(sb)
                }
            }
        }
    }
}