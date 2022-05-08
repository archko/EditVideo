package com.thuypham.ptithcm.editvideo.ui.fragment.cmd

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.FFprobeSession
import com.arthenica.ffmpegkit.LogRedirectionStrategy
import com.arthenica.ffmpegkit.SessionState
import com.thuypham.ptithcm.editvideo.R
import com.thuypham.ptithcm.editvideo.base.BaseDialogFragment
import com.thuypham.ptithcm.editvideo.databinding.FragmentCmdBinding
import com.thuypham.ptithcm.editvideo.extension.setOnSingleClickListener
import com.thuypham.ptithcm.editvideo.extension.show

class CmdDialogFragment(
) : BaseDialogFragment<FragmentCmdBinding>(R.layout.fragment_cmd) {

    override val isFullScreen = true
    private lateinit var handler: Handler

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
        }
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

    private fun runFFmpeg() {
        clearOutput()
        val ffmpegCommand = String.format("%s", binding.editCmd.text.toString())
        Log.d("TAG", String.format("Current log level is %s.", FFmpegKitConfig.getLogLevel()))
        Log.d("TAG", String.format("FFmpeg process started with arguments:\n'%s'", ffmpegCommand))
        FFmpegKit.executeAsync(ffmpegCommand,
            { session ->
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
                    handler.post { showSnackBar("Command failed. Please check output for the details.") }
                }
            }, { log ->
                handler.post { appendOutput(log.message) }
                throw AndroidRuntimeException("I am test exception thrown by the application")
            }, null
        )
        listFFmpegSessions()
    }

    private fun runFFprobe() {
        clearOutput()
        val ffprobeCommand = String.format("%s", binding.editCmd.text.toString())
        Log.d(
            "TAG",
            String.format("FFprobe process started with arguments:\n'%s'", ffprobeCommand)
        )
        val session = FFprobeSession(
            FFmpegKitConfig.parseArguments(ffprobeCommand),
            { session ->
                val state = session.state
                val returnCode = session.returnCode
                handler.post { appendOutput(session.output) }
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
                    handler.post { showSnackBar("Command failed. Please check output for the details.") }
                }
            }, null, LogRedirectionStrategy.NEVER_PRINT_LOGS
        )
        FFmpegKitConfig.asyncFFprobeExecute(session)
        listFFprobeSessions()
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
}