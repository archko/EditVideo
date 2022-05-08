package com.thuypham.ptithcm.editvideo.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.FFprobeSession
import com.arthenica.ffmpegkit.LogRedirectionStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

class CmdViewModel() : ViewModel() {

    val ffmpegResponse = MutableLiveData<FFmpegSession>()
    val ffprobeResponse = MutableLiveData<FFprobeSession>()
    val logResponse = MutableLiveData<com.arthenica.ffmpegkit.Log>()
    val handler = Handler(Looper.getMainLooper())

    suspend fun runFFmpeg(ffmpegCommand: String?): Unit = callbackFlow<FFmpegSession?> {
        FFmpegKit.executeAsync(
            ffmpegCommand,
            { session ->
                trySend(session)
            },
            { log ->
                handler.post { logResponse.value = log }
            }, null
        )

        awaitClose { }
    }.flowOn(Dispatchers.IO)
        .collectLatest {
            ffmpegResponse.value = it
        }

    suspend fun runFFprobe(ffprobeCommand: String?): Unit = callbackFlow<FFprobeSession?> {
        FFmpegKitConfig.asyncFFprobeExecute(
            FFprobeSession(
                FFmpegKitConfig.parseArguments(ffprobeCommand),
                { session ->
                    trySend(session)
                },
                { log ->
                    handler.post { logResponse.value = log }
                },
                LogRedirectionStrategy.NEVER_PRINT_LOGS
            )
        )

        awaitClose { }
    }.flowOn(Dispatchers.IO)
        .collectLatest {
            ffprobeResponse.value = it
        }
}